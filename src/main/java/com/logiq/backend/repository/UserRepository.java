package com.logiq.backend.repository;

import com.logiq.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // This is a magic Spring Boot method.
    // Just by naming it "findByEmail", Spring Boot automatically writes the SQL query for us!
    User findByEmail(String email);

}