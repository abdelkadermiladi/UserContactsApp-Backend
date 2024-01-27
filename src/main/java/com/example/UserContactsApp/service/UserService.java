package com.example.UserContactsApp.service;

import com.example.UserContactsApp.model.Contact;
import com.example.UserContactsApp.model.ApplicationUser;
import com.example.UserContactsApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("user is not valid"));

    }

    public boolean doesContactExist(ApplicationUser currentUser, Contact contact) {
        return currentUser.getContacts().stream()
                .anyMatch(existingContact ->
                        existingContact.getContactname().equals(contact.getContactname())
                                && existingContact.getEmail().equals(contact.getEmail())
                                && existingContact.getPhoneNumber().equals(contact.getPhoneNumber()));
    }
}
