package com.resumeanalyser.controller;

import com.resumeanalyser.dto.LoginRequest;
import com.resumeanalyser.dto.RegisterRequest;
import com.resumeanalyser.dto.UserResponse;
import com.resumeanalyser.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request));
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        return auth.login(request);
    }
}
