package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.User;
import dev.ctrlspace.bootcamp_2025_03.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private List<User> users;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private SendGridEmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       SendGridEmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;

        users = new ArrayList<>();
        User chris = new User(1, "Chris Sekas", "csekas@ctrlspace.dev", "123456");
        users.add(chris);
        users.add(new User(2, "Mary", "mary@gmail.com", "123456"));
        users.add(new User(3, "Nick", "Nick@gmail.com", "123456"));
        users.add(new User(4, "Alkisti", "Alkisti@gmail.com", "123456"));

//        users = List.of(chris,
//                new User(2, "Mary", "mary@gmail.com", "123456"),
//                new User(3, "Nick", "Nick@gmail.com", "123456"),
//        new User(4, "Alkisti", "Alkisti@gmail.com", "123456"));




    }

    public List<User> getUsers() {

        // TODO get users from database
//        return users;

        List<User> allUsers = userRepository.findAll();

        return allUsers;
    }

    public User getUserById(long id) throws BootcampException {

        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            return user.get();
        }
        throw new BootcampException(HttpStatus.NOT_FOUND, "User not found");

//        return userRepository
//                .findById(id)
//                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "User not found"));

    }

    public User getUserByEmail(String email) throws BootcampException {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            return user.get();
        }
        throw new BootcampException(HttpStatus.NOT_FOUND, "User not found");
    }

    public User create(User user) throws BootcampException {
        if (user.getId() != null) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "User id must be null");
        }

        if (user.getName() == null || user.getName().isEmpty()) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "User name must not be null or empty");
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "User email must not be null or empty");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BootcampException(HttpStatus.CONFLICT, "Email already in use");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "User password must not be null or empty");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerified(false);

        user = userRepository.save(user);

        try {
            emailService.sendVerificationEmail(user.getEmail(), token);
        } catch (IOException e) {
            throw new BootcampException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send verification email");
        }

        return user;
    }

    public User updateById(Long userIdToUpdate, User newUser) throws BootcampException {

        User existingUser = getUserById(userIdToUpdate);

        // Update existing user with the new user details
        if (newUser.getName() != null) {
            existingUser.setName(newUser.getName());
        }
        if (newUser.getEmail() != null) {
            existingUser.setEmail(newUser.getEmail());
        }
        if (newUser.getPassword() != null) {
            existingUser.setPassword(newUser.getPassword());
        }

        existingUser = userRepository.save(existingUser);

        return existingUser;
    }

    public User deleteById(long id) throws BootcampException {
        User deletedUser = getUserById(id);
//        userRepository.delete(deletedUser);
        userRepository.deleteById(id);


        return deletedUser;
    }


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;
        try {
            user = this.getUserByEmail(username);
            return user;
        } catch (BootcampException e) {
            throw new UsernameNotFoundException("User not found with username: " + username, e);
        }

    }

    public User getUserById(Long userId) throws BootcampException {
        System.out.println("Fetching user with ID: " + userId);
        return userRepository.findById(userId).orElseThrow(() ->
                new BootcampException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) throws BootcampException {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean verifyToken(String token) throws BootcampException {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        user.setVerified(true);
        user.setVerificationToken(null); // Optional: clear the token
        userRepository.save(user);
        return true;
    }

    public void initiatePasswordReset(String email) throws BootcampException {
        User user = getUserByEmail(email);

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        log.info("Saving reset token: {} for user {}", resetToken, user.getEmail());
        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        } catch (IOException e) {
            throw new BootcampException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send password reset email");
        }
    }

    public void resetPassword(String token, String newPassword) throws BootcampException {
        Optional<User> userOptional = userRepository.findByPasswordResetToken(token);

        if (userOptional.isEmpty()) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Invalid or expired password reset token");
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null); // invalidate token
        userRepository.save(user);
    }
}
