package com.example.chatonlineback.repository;

import com.example.chatonlineback.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
