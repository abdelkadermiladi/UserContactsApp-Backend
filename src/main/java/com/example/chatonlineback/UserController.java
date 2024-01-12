package com.example.chatonlineback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<User>> findAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("start");
        for(int i=0;i<users.size();i++){
            System.out.println(users.get(i).getUsername());
            System.out.println(users.get(i).getPassword());
        }
        System.out.println("end");

        return ResponseEntity.ok(users);
    }
    /*
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User newUser) {
        // Validate and save the new user
        User savedUser = userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
    */
    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {

        User user = userRepository.findByUsername(username);

        if (user != null) {
            System.out.println(user.getGender());
            return ResponseEntity.ok(user);
        } else {
            // User not found
            return ResponseEntity.notFound().build();
        }
    }


}
