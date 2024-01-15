package com.example.chatonlineback.repository;

import com.example.chatonlineback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

}
