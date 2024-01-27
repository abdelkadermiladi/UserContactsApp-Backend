package com.example.UserContactsApp.controller;

import com.example.UserContactsApp.model.LoginRequest;
import com.example.UserContactsApp.model.LoginResponseDTO;
import com.example.UserContactsApp.model.RegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.UserContactsApp.model.ApplicationUser;
import com.example.UserContactsApp.service.AuthenticationService;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/register")
    public ApplicationUser registerUser(@RequestBody RegistrationRequest body){
        return authenticationService.registerUser(
                body.getUsername(), body.getPassword(),body.getAge(), body.getGender(), body.getEmail(), body.getPhone_number());
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(@RequestBody LoginRequest body) {
        LoginResponseDTO loginResponse = authenticationService.loginUser(body.getUsername(), body.getPassword());

        if (loginResponse.getUser() != null && !loginResponse.getJwt().isEmpty()) {
            return ResponseEntity.ok(loginResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}