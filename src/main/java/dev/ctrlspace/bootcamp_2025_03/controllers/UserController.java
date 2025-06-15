package dev.ctrlspace.bootcamp_2025_03.controllers;


import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.TokenDTO;
import dev.ctrlspace.bootcamp_2025_03.model.User;
import dev.ctrlspace.bootcamp_2025_03.model.dto.UserProfileSettingsDTO;
import dev.ctrlspace.bootcamp_2025_03.services.UserProfileSettingsService;
import dev.ctrlspace.bootcamp_2025_03.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;
    private JwtEncoder jwtEncoder;

    @Autowired
    public UserController(UserService userService, JwtEncoder jwtEncoder) {
        this.userService = userService;
        this.jwtEncoder = jwtEncoder;
    }

    @Autowired
    private UserProfileSettingsService profileSettingsService;

    @GetMapping(value = "/{id}")
    public User getUserById(@PathVariable("id") long id, Authentication authentication) throws BootcampException {

        Jwt jwt = (Jwt) authentication.getPrincipal();  // Cast principal to Jwt
        Long authenticatedUserId = Long.valueOf(jwt.getClaimAsString("sub")); // extract "sub" claim

        if (!authenticatedUserId.equals(id)) {
            throw new BootcampException(HttpStatus.FORBIDDEN, "You do not have permission to update this user");
        }

        return userService.getUserById(id);
    }

    @GetMapping(value = "", params = "id")
    public User getUserByParamId(@RequestParam long id, Authentication authentication) throws BootcampException {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long authenticatedUserId = Long.valueOf(jwt.getClaimAsString("sub"));

        if (!authenticatedUserId.equals(id)) {
            throw new BootcampException(HttpStatus.FORBIDDEN, "You do not have permission to update this user");
        }

        return userService.getUserById(id);
    }

    @GetMapping("")
    public User getCurrentUser(Authentication authentication) throws BootcampException {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long authenticatedUserId = Long.valueOf(jwt.getClaimAsString("sub"));

        return userService.getUserById(authenticatedUserId);
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam("token") String token) throws BootcampException {
        boolean verified = userService.verifyToken(token);

        if (verified) {
            return "Email verified! Redirecting to the login page...";
        } else {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Invalid or expired token.");
        }
    }

    @PostMapping(value = "")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) throws BootcampException {

        User createdUser = userService.create(user);

        return createdUser;
    }

    @PostMapping("/login")
    public TokenDTO login(Authentication authentication) throws BootcampException {
        String email = authentication.getName();
        User loggedInUser = userService.getUserByEmail(email);

        // Check if user exists
        if (loggedInUser == null) {
            throw new BootcampException(HttpStatus.UNAUTHORIZED, "User not found.");
        }

        // Check if user is verified
        if (!loggedInUser.isVerified()) {
            throw new BootcampException(HttpStatus.UNAUTHORIZED, "Please verify your email before logging in.");
        }

        // Generate JWT token (If user is valid and verified)
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(6, ChronoUnit.HOURS))
                .subject(String.valueOf(loggedInUser.getId()))
                .claim("user_id", loggedInUser.getId())
                .build();

        // Encode and return token
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);

        return tokenDTO;
    }

    @PutMapping(value = "/{id}")
    public User updateUser(@PathVariable ("id") long id, @RequestBody User user, Authentication authentication) throws BootcampException {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long authenticatedUserId = Long.valueOf(jwt.getClaimAsString("sub"));

        if (!authenticatedUserId.equals(id)) {
            throw new BootcampException(HttpStatus.FORBIDDEN, "You do not have permission to update this user");
        }

        if (user.getId() != null && user.getId() != id) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "User id must be the same as the path variable");
        }

        User updatedUser = userService.updateById(id, user);

        return updatedUser;
    }

    @DeleteMapping(value = "/{id}")
    public User deleteUser(@PathVariable ("id") long id, Authentication authentication) throws BootcampException {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long authenticatedUserId = Long.valueOf(jwt.getClaimAsString("sub"));

        if (!authenticatedUserId.equals(id)) {
            throw new BootcampException(HttpStatus.FORBIDDEN, "You do not have permission to update this user");
        }

        User deletedUser = userService.deleteById(id);

        return deletedUser;


    }

    @PatchMapping("/{id}/change-password")
    public Map<String, String> changePassword(
            @PathVariable("id") long userId,
            @RequestBody Map<String, String> payload,
            Authentication authentication) throws BootcampException {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long authenticatedUserId = Long.valueOf(jwt.getClaimAsString("sub"));

        if (!authenticatedUserId.equals(userId)) {
            throw new BootcampException(HttpStatus.FORBIDDEN, "You do not have permission to change this user's password");
        }

        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Old password cannot be empty.");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "New password cannot be empty.");
        }

        userService.changePassword(authenticatedUserId, oldPassword, newPassword);

        return Map.of("message", "Password changed successfully.");
    }

    @PostMapping("/password-reset-request")
    public void requestPasswordReset(@RequestBody Map<String, String> payload) throws BootcampException {
        String email = payload.get("email");
        userService.initiatePasswordReset(email);
    }

    @PostMapping("/password-reset")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> payload) throws BootcampException {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");
        userService.resetPassword(token, newPassword);

        return Map.of("message", "Password reset successful. You can now log in with your new password.");
    }

    @GetMapping("/password-reset")
    public String showResetInstructions() {
        return "Your password reset link is valid. You can now close this page and proceed to submitting your new password.";
    }

    @GetMapping("/{id}/profile-settings")
    public UserProfileSettingsDTO getProfileSettings(
            @PathVariable Long id,
            Authentication authentication) throws BootcampException {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long authenticatedUserId = Long.valueOf(jwt.getClaimAsString("sub"));

        if (!authenticatedUserId.equals(id)) {
            throw new BootcampException(HttpStatus.FORBIDDEN, "You do not have permission to view this user's profile settings");
        }

        return profileSettingsService.getProfileSettingsByUserId(id);
    }

    @PutMapping("/{id}/profile-settings")
    public UserProfileSettingsDTO updateProfileSettings(
            @PathVariable Long id,
            @RequestBody UserProfileSettingsDTO dto,
            Authentication authentication) throws BootcampException {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long authenticatedUserId = Long.valueOf(jwt.getClaimAsString("sub"));

        if (!authenticatedUserId.equals(id)) {
            throw new BootcampException(HttpStatus.FORBIDDEN, "You do not have permission to update this user's profile settings");
        }

        return profileSettingsService.updateProfileSettings(id, dto);
    }
}
