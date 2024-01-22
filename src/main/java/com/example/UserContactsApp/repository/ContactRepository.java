package com.example.UserContactsApp.repository;

import com.example.UserContactsApp.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

}
