package com.example.UserContactsApp.controller;

import com.example.UserContactsApp.model.*;
import com.example.UserContactsApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserRepository userRepository;
    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    /////////////////////////////////////////////////////////////////////////////////////

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/users")
    public ResponseEntity<List<ApplicationUser>> findAllUsers() {
        List<ApplicationUser> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    /////////////////////////////////////////////////////////////////////////////////////

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/users/{username}")
    public ResponseEntity<ApplicationUser> getUserByUsername(@PathVariable String username) {
        Optional<ApplicationUser> userOptional = userRepository.findByUsername(username);

        ApplicationUser user = userOptional.orElse(null);

        if (user != null) {
            userRepository.save(user);
            return ResponseEntity.ok(user);
        } else {
            // User not found
            return ResponseEntity.notFound().build();
        }
    }


}
