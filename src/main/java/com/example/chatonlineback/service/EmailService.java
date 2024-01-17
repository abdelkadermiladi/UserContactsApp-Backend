package com.example.chatonlineback.service;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    /*
    private boolean isValidEmail(String email) {
        try {
            // Using JavaMail's InternetAddress to check if the email is in a valid format
            new InternetAddress(email).validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }*/
    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(String from, String to, String subject, String content) {
        //if (isValidEmail(to)) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(from);
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(content);
            javaMailSender.send(mailMessage);
        //} else {
            // Handle invalid email address (log, throw exception, etc.)
            //System.out.println("Invalid email address: " + to);
        //}
    }
}
