package com.example.UserContactsApp.service;

import com.example.UserContactsApp.model.Contact;
import com.example.UserContactsApp.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public boolean doesContactExist(User currentUser, Contact contact) {
        return currentUser.getContacts().stream()
                .anyMatch(existingContact ->
                        existingContact.getContactname().equals(contact.getContactname())
                                && existingContact.getEmail().equals(contact.getEmail())
                                && existingContact.getPhoneNumber().equals(contact.getPhoneNumber()));
    }
}
