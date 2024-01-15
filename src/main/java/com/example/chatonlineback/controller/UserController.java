package com.example.chatonlineback.controller;

import com.example.chatonlineback.model.*;
import com.example.chatonlineback.repository.ContactRepository;
import com.example.chatonlineback.service.EmailService;

import com.example.chatonlineback.repository.UserRepository;
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
    private final ContactRepository contactRepository;
    private final EmailService emailService;
    private final AuthenticatedUser authenticatedUser;
    @Autowired
    public UserController(UserRepository userRepository,ContactRepository contactRepository,EmailService emailService) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.emailService  =emailService;
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
 /*
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/add-contact/{contactname}")
    public ResponseEntity<String> addContact(@PathVariable String contactname) {
        //User currentUser = authenticatedUser.getAuthenticatedUser();
        User currentUser = userRepository.findByUsername("abdelkader");
        Contact contact_to_add = contactRepository.findByContactname(contactname);

        if (currentUser != null && contact_to_add != null) {
            if (currentUser.addContact(contact_to_add)) {
                userRepository.save(currentUser);
                // Access the set of contacts for the user
                Set<Contact> userContacts = currentUser.getContacts();
                // Now you can iterate over the set or perform other operations with the contacts
                for (Contact contact : userContacts) {
                    // Do something with the contact
                    System.out.println("Contact Phone Number: " + contact.getPhoneNumber());
                }
                // Notify otherUser about the addition (send a notification)
                return ResponseEntity.ok("Contact added successfully");
            } else {
                return ResponseEntity.status(400).body("Contact already exists");
            }
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }
*/
    /////////////////////////////////////////////////////////////////////////////////////

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/add-contact")
    public ResponseEntity<Map<String, String>> addContact(@RequestBody Contact contact) {
        User currentUser = authenticatedUser.getAuthenticatedUser();

        if (currentUser != null && contact != null) {
            contactRepository.save(contact);

            if (currentUser.addContact(contact)) {

                userRepository.save(currentUser);

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
