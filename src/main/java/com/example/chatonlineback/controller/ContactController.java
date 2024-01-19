/*

package com.example.chatonlineback.controller;

import com.example.chatonlineback.model.Contact;
import com.example.chatonlineback.repository.ContactRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api")
public class ContactController {
    private final ContactRepository contactRepository;

    @Autowired
    public ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;

    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/contacts")
    public ResponseEntity<List<Contact>> findAllContacts() {
        List<Contact> contacts = contactRepository.findAll();
        return ResponseEntity.ok(contacts);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/contacts/{contactname}")
    public ResponseEntity<Contact> getContactByContactname(@PathVariable String contactname) {

        Contact contact = contactRepository.findByContactname(contactname);

        if (contact != null) {
            return ResponseEntity.ok(contact);
        } else {
            // Contact not found
            return ResponseEntity.notFound().build();
        }
    }

}

*/