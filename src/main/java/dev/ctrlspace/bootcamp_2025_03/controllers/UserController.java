package dev.ctrlspace.bootcamp_2025_03.controllers;


import dev.ctrlspace.bootcamp_2025_03.model.User;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {


    private List<User> users;

    public UserController() {
        users = new ArrayList<>();
        User chris = new User(1, "Chris Sekas", "csekas@ctrlspace.dev", "123456");
        users.add(chris);
        users.add(new User(2, "Mary", "mary@gmail.com", "123456"));
        users.add(new User(3, "Nick", "Nick@gmail.com", "123456"));
        users.add(new User(4, "Alkisti", "Alkisti@gmail.com", "123456"));

    }



    @GetMapping("/users")
    public List<User> getUser() {
        return users;
    }

    //    HTTP GET /users/1   <- Rest APIs
    @GetMapping(value = "/users/{id}")
    public User getUserById(@PathVariable long id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

//    HTTP GET /users?id=1
    @GetMapping(value = "/users", params = "id")
    public User getUserByParamId(@RequestParam long id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    @PostMapping(value = "/users")
    public User createUser(@RequestBody User user) {
//        TODO save user
        return null;
    }

    @PutMapping(value = "/users/{id}")
    public User updateUser(@PathVariable long id, @RequestBody User user) {
//        TODO update user
        return null;
    }

    @DeleteMapping(value = "/users/{id}")
    public void deleteUser(@PathVariable long id) {
//        TODO delete user

    }

}
