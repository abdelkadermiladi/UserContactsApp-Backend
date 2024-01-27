package com.example.UserContactsApp;

import com.example.UserContactsApp.model.ApplicationUser;
import com.example.UserContactsApp.model.Role;
import com.example.UserContactsApp.repository.RoleRepository;
import com.example.UserContactsApp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class ChatonlineBackApplication {

    public static void main(String[] args) {

        SpringApplication.run(ChatonlineBackApplication.class, args);
    }
    @Bean
    CommandLineRunner run(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncode){
        return args ->{
            if(roleRepository.findByAuthority("ADMIN").isPresent()) return;
            Role adminRole = roleRepository.save(new Role("ADMIN"));
            roleRepository.save(new Role("USER"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            ApplicationUser admin = new ApplicationUser(1L, "admin", passwordEncode.encode("password"), 20,"Male", 12346789, "abdelkader@admin.tn", roles);
            userRepository.save(admin);
        };
    }

}
