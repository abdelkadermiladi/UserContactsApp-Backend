package com.example.chatonlineback.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private Integer age;
    private String gender;
    private Integer phoneNumber;
    private String email;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_contacts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private Set<Contact> contacts = new HashSet<>();


    public Set<Contact> getContacts() {
        return contacts;
    }

    public boolean addContact(Contact contact) {
        boolean ret = false;
         ret = contacts.add(contact);
        return ret;
    }

    public void removeContact(Contact contact) {
        contacts.remove(contact);
    }

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_notifications",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "notification_id")
    )
    private Set<Notification> notifications = new HashSet<>();

    public Set<Notification> getNotifications() {
        return notifications;
    }
    // Existing methods

    @Transactional
    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Integer phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
