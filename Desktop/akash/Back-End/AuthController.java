package com.logiq.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/api/auth/health")
    public String authHealth() {
        return "Auth module placeholder";
    }
}