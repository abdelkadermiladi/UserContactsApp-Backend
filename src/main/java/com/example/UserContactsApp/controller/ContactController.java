package com.example.UserContactsApp.controller;

import com.example.UserContactsApp.model.ApplicationUser;
import com.example.UserContactsApp.model.Contact;
import com.example.UserContactsApp.repository.ContactRepository;
import com.example.UserContactsApp.repository.UserRepository;
import com.example.UserContactsApp.service.EmailService;
import com.example.UserContactsApp.service.UserService;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ContactController {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final EmailService emailService;
    private final UserService userService;

    @Autowired
    public ContactController(UserRepository userRepository,ContactRepository contactRepository,
                          EmailService emailService,UserService userService) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.emailService  =emailService;
        this.userService  =userService;
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/list-contacts")
    public ResponseEntity<Set<Contact>> listContacts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String authenticatedUserName = authentication.getName();

            Optional<ApplicationUser> userOptional = userRepository.findByUsername(authenticatedUserName);

            ApplicationUser authenticatedUser = userOptional.orElse(null);


            if (authenticatedUser != null) {
                Set<Contact> userContacts = authenticatedUser.getContacts();
                return ResponseEntity.ok(userContacts);
            } else {
                return ResponseEntity.status(404).body(null);
            }
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String authenticatedUserName = authentication.getName();

            Optional<ApplicationUser> userOptional = userRepository.findByUsername(authenticatedUserName);

            ApplicationUser authenticatedUser = userOptional.orElse(null);

            if (authenticatedUser != null && contact != null) {
                // Vérifier si le contact existe déjà
                if (userService.doesContactExist(authenticatedUser, contact)) {
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

                if (authenticatedUser.addContact(contact)) {
                    userRepository.save(authenticatedUser);

                    String contactEmail = contact.getEmail();
                    String subject = "Ajout Contact";
                    String content = authenticatedUser.getUsername() + " vous a ajouté à sa liste des contacts :\n" +
                            "Nom: " + contact.getContactname() + "\nNuméro: " + contact.getPhoneNumber();

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
                response.put("error", "User not found or invalid contact");
                return ResponseEntity.status(404).body(response);
            }
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(401).body(response);
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String authenticatedUserName = authentication.getName();

            Optional<ApplicationUser> userOptional = userRepository.findByUsername(authenticatedUserName);

            ApplicationUser authenticatedUser = userOptional.orElse(null);

            if (authenticatedUser != null) {
                Optional<Contact> contactToRemove = authenticatedUser.getContacts().stream()
                        .filter(contact -> contact.getId().equals(contactId))
                        .findFirst();

                if (contactToRemove.isPresent()) {
                    authenticatedUser.removeContact(contactToRemove.get());
                    userRepository.save(authenticatedUser);
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String authenticatedUserName = authentication.getName();

            Optional<ApplicationUser> userOptional = userRepository.findByUsername(authenticatedUserName);

            ApplicationUser authenticatedUser = userOptional.orElse(null);

            if (authenticatedUser != null) {
                Optional<Contact> contactToUpdate = authenticatedUser.getContacts().stream()
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
                    userRepository.save(authenticatedUser);

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
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "User not authenticated");
            return ResponseEntity.status(401).body(response);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
}
