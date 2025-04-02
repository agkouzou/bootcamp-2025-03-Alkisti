package dev.ctrlspace.bootcamp_2025_03.controllers;


import dev.ctrlspace.bootcamp_2025_03.model.User;
import dev.ctrlspace.bootcamp_2025_03.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> getUser() {
        return userService.getUsers();
    }

    //    HTTP GET /users/1   <- Rest APIs
    @GetMapping(value = "/users/{id}")
    public User getUserById(@PathVariable long id) {
        return userService.getUserById(id);

    }

//    HTTP GET /users?id=1
    @GetMapping(value = "/users", params = "id")
    public User getUserByParamId(@RequestParam long id) {
        return userService.getUserById(id);
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
