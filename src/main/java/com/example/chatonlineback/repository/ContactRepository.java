package com.example.chatonlineback.repository;

import com.example.chatonlineback.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

}
