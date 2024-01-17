package com.example.chatonlineback.controller;

import com.example.chatonlineback.model.*;
import com.example.chatonlineback.repository.ContactRepository;
import com.example.chatonlineback.service.EmailService;

import com.example.chatonlineback.repository.UserRepository;
import com.example.chatonlineback.service.UserService;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


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

    //@Value("${spring.mail.username}")
    //private String EmailSender;
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
            // Check if the email is valid
            if (!isValidEmail(contact.getEmail())) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Invalid email address");
                return ResponseEntity.status(400).body(response);
            }

            contactRepository.save(contact);

            if (currentUser.addContact(contact)) {
                userRepository.save(currentUser);

                String contactEmail = contact.getEmail();
                String subject = "Ajout Contact";
                String content = currentUser.getUsername()+" vous a ajouté a sa liste des contacts :\n" +
                        "Nom: "+contact.getContactname()+"\nNuméro: "+contact.getPhoneNumber();

                // Send email
                emailService.sendEmail(EmailSender, contactEmail, subject, content);

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
    private boolean isValidEmail(String email) {
        try {
            new InternetAddress(email).validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////
    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping("/remove-contact/{contactname}")
    public ResponseEntity<Map<String, String>> removeContact(@PathVariable String contactname) {
        User currentUser = authenticatedUser.getAuthenticatedUser();

        if (currentUser != null) {
            Optional<Contact> contactToRemove = currentUser.getContacts().stream()
                    .filter(contact -> contact.getContactname().equals(contactname))
                    .findFirst();

            if (contactToRemove.isPresent()) {
                currentUser.removeContact(contactToRemove.get());
                userRepository.save(currentUser);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Contact removed successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Contact not found");
                return ResponseEntity.status(404).body(response);
            }
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(401).body(response);
        }
    }



}
