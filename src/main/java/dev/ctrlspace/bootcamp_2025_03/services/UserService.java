package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private List<User> users;

    public UserService() {
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
        return users;
    }

    public User getUserById(long id) throws BootcampException {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
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

        user.setId(users.size() + 1L);

        users.add(user);
        return user;
    }

    public User updateById(Long userIdToUpdate, User newUser) throws BootcampException {

        User existingUser = getUserById(userIdToUpdate);

        // Update existing user with the new user details
        existingUser.setName(newUser.getName());
        existingUser.setEmail(newUser.getEmail());
        existingUser.setPassword(newUser.getPassword());

//        userRapository.save(existingUser);

        return existingUser;
    }

    public User deleteById(long id) throws BootcampException {
//        userRepository.deleteById(id);

        User deletedUser = getUserById(id);

        users.remove(deletedUser);

        return deletedUser;
    }
}
