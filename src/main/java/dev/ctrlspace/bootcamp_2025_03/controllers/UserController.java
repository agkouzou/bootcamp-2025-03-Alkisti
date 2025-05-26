package dev.ctrlspace.bootcamp_2025_03.controllers;


import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.TokenDTO;
import dev.ctrlspace.bootcamp_2025_03.model.User;
import dev.ctrlspace.bootcamp_2025_03.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private UserService userService;
    private JwtEncoder jwtEncoder;

    @Autowired
    public UserController(UserService userService, JwtEncoder jwtEncoder) {
        this.userService = userService;
        this.jwtEncoder = jwtEncoder;
    }

    @GetMapping("")
    public List<User> getUsers() {
        logger.debug("Get all users from UserController");
        return userService.getUsers();
    }

    //    HTTP GET /users/1   <- Rest APIs
//    With Response Entity
//    @GetMapping(value = "/{id}")
//    public ResponseEntity<User> getUserById(@PathVariable long id) {
//
//        User u = null;
//        try {
//            u = userService.getUserById(id);
//        } catch (BootcampException e) {
//            return ResponseEntity.notFound().build();
//        }
//
//        return ResponseEntity.status(HttpStatus.OK).body(u);
//
//    }

    @GetMapping(value = "/{id}")
    public User getUserById(@PathVariable("id") long id, Authentication authentication) throws BootcampException {
        return userService.getUserById(id);
    }

//    HTTP GET /users?id=1
    @GetMapping(value = "", params = "id")
    public User getUserByParamId(@RequestParam long id) throws BootcampException {
        return userService.getUserById(id);
    }

    @PostMapping(value = "")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) throws BootcampException {

        User createdUser = userService.create(user);

        return createdUser;
    }

    @PostMapping("/login")
    public TokenDTO login(Authentication authentication) throws BootcampException {

        User loggedInUser = ((User)authentication.getPrincipal());
        // 2) Build JWT claims
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(6, ChronoUnit.HOURS))
                .subject(String.valueOf(loggedInUser.getId()))
//                .subject(loggedInUser.getUsername())
//                .claim("roles", auth.getAuthorities().stream()
//                        .map(a -> a.getAuthority()).toList())
                .build();

        // 3) Encode & return token
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);
        return tokenDTO;
    }

    @PutMapping(value = "/{id}")
    public User updateUser(@PathVariable ("id") long id, @RequestBody User user) throws BootcampException {

        if (user.getId() != null && user.getId() != id) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "User id must be the same as the path variable");
        }

        User updatedUser = userService.updateById(id, user);

        return updatedUser;
    }

    @DeleteMapping(value = "/{id}")
    public User deleteUser(@PathVariable ("id") long id) throws BootcampException {

        User deletedUser = userService.deleteById(id);

        return deletedUser;


    }

}
