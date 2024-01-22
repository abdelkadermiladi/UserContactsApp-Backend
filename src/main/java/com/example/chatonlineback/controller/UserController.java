package com.example.chatonlineback.controller;

import com.example.chatonlineback.model.*;
import com.example.chatonlineback.repository.ContactRepository;
import com.example.chatonlineback.repository.NotificationRepository;
import com.example.chatonlineback.service.EmailService;
import com.example.chatonlineback.repository.UserRepository;
import com.example.chatonlineback.service.UserService;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final UserService userService;
    private final AuthenticatedUser authenticatedUser;
    @Autowired
    public UserController(UserRepository userRepository,ContactRepository contactRepository,
                          NotificationRepository notificationRepository,
                          EmailService emailService,UserService userService) {
        this.contactRepository = contactRepository;
        this.notificationRepository = notificationRepository;
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
            userRepository.save(user);
            return ResponseEntity.ok(user);
        } else {
            // User not found
            return ResponseEntity.notFound().build();
        }
    }
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/users/{username}")
    public ResponseEntity<User> addNotification(@PathVariable String username) {
        User currentuser = userRepository.findByUsername(username);

        if (currentuser != null && authenticatedUser.getAuthenticatedUser() != null) {
            Notification notification = new Notification();
            notification.setMessage("Your profile has been viewed by " + authenticatedUser.getAuthenticatedUser().getUsername());
            notification.setTime(LocalDateTime.now());
            notification.setUser(currentuser);
            currentuser.addNotification(notification);

            userRepository.save(currentuser);
            notificationRepository.save(notification);

            return ResponseEntity.ok(currentuser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/notifications")
    public ResponseEntity<Set<Notification>> getUserNotifications() {
        User currentUser = authenticatedUser.getAuthenticatedUser();

        if (currentUser != null) {
            Set<Notification> notifications = currentUser.getNotifications();
            return ResponseEntity.ok(notifications);
        } else {
            return ResponseEntity.status(401).body(null); // User not authenticated
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
    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/list-contacts")
    public ResponseEntity<Set<Contact>> listContacts() {
        User currentUser = authenticatedUser.getAuthenticatedUser();

        if (currentUser != null) {
            Set<Contact> userContacts = currentUser.getContacts();
            return ResponseEntity.ok(userContacts);
        } else {
            return ResponseEntity.status(404).body(null);
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////

    @Value("${spring.mail.username}")
    private String EmailSender;
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
    @DeleteMapping("/remove-contact/{contactId}")
    public ResponseEntity<Map<String, String>> removeContact(@PathVariable Long contactId) {
        User currentUser = authenticatedUser.getAuthenticatedUser();

        if (currentUser != null) {
            Optional<Contact> contactToRemove = currentUser.getContacts().stream()
                    .filter(contact -> contact.getId().equals(contactId))
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
    /////////////////////////////////////////////////////////////////////////////////////
    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping("/update-contact/{contactId}")
    public ResponseEntity<Map<String, String>> updateContact(
            @PathVariable Long contactId,
            @RequestBody Contact updatedContact
    ) {
        User currentUser = authenticatedUser.getAuthenticatedUser();

        if (currentUser != null) {
            Optional<Contact> contactToUpdate = currentUser.getContacts().stream()
                    .filter(contact -> contact.getId().equals(contactId))
                    .findFirst();

            if (contactToUpdate.isPresent()) {
                // Update the contact details
                Contact existingContact = contactToUpdate.get();
                existingContact.setContactname(updatedContact.getContactname());
                existingContact.setEmail(updatedContact.getEmail());
                existingContact.setPhoneNumber(updatedContact.getPhoneNumber());

                // Save the updated contact
                contactRepository.save(existingContact);
                userRepository.save(currentUser);

                Map<String, String> response = new HashMap<>();
                response.put("message", "Contact updated successfully");
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

    /////////////////////////////////////////////////////////////////////////////////////
}
