package com.example.chatonlineback.controller;

import com.example.chatonlineback.model.*;
import com.example.chatonlineback.repository.ContactRepository;
import com.example.chatonlineback.service.EmailService;

import com.example.chatonlineback.repository.UserRepository;
import com.example.chatonlineback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final EmailService emailService;
    private final UserService userService;
    private final AuthenticatedUser authenticatedUser;
    @Autowired
    public UserController(UserRepository userRepository,ContactRepository contactRepository,
                          EmailService emailService,UserService userService) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.emailService  =emailService;
        this.userService  =userService;
        this.authenticatedUser = new AuthenticatedUser(); // Initialize the object
    }

    /////////////////////////////////////////////////////////////////////////////////////

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/users")
    public ResponseEntity<List<User>> findAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    /////////////////////////////////////////////////////////////////////////////////////

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
    /////////////////////////////////////////////////////////////////////////////////////

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
            authenticatedUser.setAuthenticatedUser(user);
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
    /////////////////////////////////////////////////////////////////////////////////////

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
    /////////////////////////////////////////////////////////////////////////////////////

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/list-contacts")
    public ResponseEntity<Set<Contact>> listContacts() {
        User currentUser = authenticatedUser.getAuthenticatedUser();

        if (currentUser != null) {
            Set<Contact> userContacts = currentUser.getContacts();
            return ResponseEntity.ok(userContacts);
        } else {
            return ResponseEntity.status(404).body(null); // Utilisateur non trouvé
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/add-contact")
    public ResponseEntity<Map<String, String>> addContact(@RequestBody Contact contact) {
        User currentUser = authenticatedUser.getAuthenticatedUser();

        if (currentUser != null && contact != null) {
            // Vérifier si le contact existe déjà
            if (userService.doesContactExist(currentUser, contact)) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Contact already exists");
                return ResponseEntity.status(400).body(response);
            }

            contactRepository.save(contact);

            if (currentUser.addContact(contact)) {
                userRepository.save(currentUser);

                // Notify otherUser about the addition (send a notification)
                Map<String, String> response = new HashMap<>();
                response.put("message", "Contact added successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Contact already exists");
                return ResponseEntity.status(400).body(response);
            }
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "User not found");
            return ResponseEntity.status(404).body(response);
        }
    }


}
