package com.example.chatonlineback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;
    private final EmailService emailService;


    @Autowired
    public UserController(UserRepository userRepository,EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService  =emailService;
    }



    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/users")
    public ResponseEntity<List<User>> findAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/users/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {

        User user = userRepository.findByUsername(username);

        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            // User not found
            return ResponseEntity.notFound().build();
        }
    }
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        // Extract username and password from the request
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // Validate the credentials against the database
        User user = userRepository.findByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            // Authentication successful
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        } else {
            // Authentication failed
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }
    }

    @Value("${spring.mail.username}")
    private String EmailSender;
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest emailRequest) {
        // Extract recipient, subject, and content from the request
        String recipientUsername = emailRequest.getRecipientUsername();
        String subject = emailRequest.getSubject();
        String content = emailRequest.getContent();

        // Fetch sender and recipient details from the database
        User recipient = userRepository.findByUsername(recipientUsername);

        if (recipient != null) {
            // Send email
            emailService.sendEmail(EmailSender, recipient.getEmail(), subject, content);

            return ResponseEntity.ok("Email sent successfully");
        } else {
            // recipient not found
            return ResponseEntity.status(404).body("Recipient not found");
        }
    }

}
