package com.example.UserContactsApp.repository;

import com.example.UserContactsApp.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
