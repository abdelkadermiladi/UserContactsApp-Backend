package com.example.UserContactsApp.controller;

import com.example.UserContactsApp.model.ApplicationUser;
import com.example.UserContactsApp.model.Notification;
import com.example.UserContactsApp.repository.NotificationRepository;
import com.example.UserContactsApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;


    @Autowired
    public NotificationController(UserRepository userRepository,
                          NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }


    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/users/{username}")
    public ResponseEntity<ApplicationUser> addNotification(@PathVariable String username) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String authenticatedUserName = authentication.getName();

            Optional<ApplicationUser> userOptional = userRepository.findByUsername(username);

            ApplicationUser currentuser = userOptional.orElse(null);

            if (currentuser != null && authenticatedUserName != null) {
                Notification notification = new Notification();
                notification.setMessage("Your profile has been viewed by " + authenticatedUserName);
                notification.setTime(LocalDateTime.now());
                notification.setUser(currentuser);
                currentuser.addNotification(notification);

                userRepository.save(currentuser);
                notificationRepository.save(notification);
                return ResponseEntity.ok(currentuser);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Return UNAUTHORIZED if not authenticated
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/notifications")
    public ResponseEntity<Set<Notification>> getUserNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String authenticatedUserName = authentication.getName();

            Optional<ApplicationUser> userOptional = userRepository.findByUsername(authenticatedUserName);

            ApplicationUser authenticatedUser = userOptional.orElse(null);

            if (authenticatedUser != null) {
                Set<Notification> notifications = authenticatedUser.getNotifications();
                return ResponseEntity.ok(notifications);
            } else {
                return ResponseEntity.status(401).body(null); // User not authenticated
            }
        } else {
            return ResponseEntity.status(401).body(null); // User not authenticated
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
}
