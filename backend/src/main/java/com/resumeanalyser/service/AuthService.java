package com.resumeanalyser.service;

import com.resumeanalyser.dto.LoginRequest;
import com.resumeanalyser.dto.RegisterRequest;
import com.resumeanalyser.dto.UserResponse;
import com.resumeanalyser.entity.User;
import com.resumeanalyser.exception.ApiException;
import com.resumeanalyser.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        String name = req.fullName() == null ? "" : req.fullName().trim();
        String email = normalise(req.email());
        if (name.length() < 2) throw bad("Full name is required.");
        if (!EMAIL.matcher(email).matches()) throw bad("Please enter a valid email address.");
        String problem = passwordProblem(req.password());
        if (problem != null) throw bad(problem);
        if (!req.password().equals(req.confirmPassword())) throw bad("Password and confirm password do not match.");
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }
        User u = createUser(name, email, req.password());
        return toResponse(u);
    }

    /** Used by register() and by the demo data seeder. */
    @Transactional
    public User createUser(String fullName, String email, String rawPassword) {
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(normalise(email));
        u.setPassword(encoder.encode(rawPassword));
        return users.save(u);
    }

    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest req) {
        String email = normalise(req.email());
        if (email.isEmpty() || req.password() == null || req.password().isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
        User u = users.findByEmailIgnoreCase(email)
                .filter(found -> encoder.matches(req.password(), found.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        return toResponse(u);
    }

    private static UserResponse toResponse(User u) {
        return new UserResponse(u.getUserId(), u.getFullName(), u.getEmail());
    }

    private static String normalise(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static ApiException bad(String msg) {
        return new ApiException(HttpStatus.BAD_REQUEST, msg);
    }

    static String passwordProblem(String p) {
        if (p == null || p.length() < 8) return "Password must be at least 8 characters.";
        boolean up = false, low = false, digit = false;
        for (char c : p.toCharArray()) {
            if (Character.isUpperCase(c)) up = true;
            else if (Character.isLowerCase(c)) low = true;
            else if (Character.isDigit(c)) digit = true;
        }
        return (up && low && digit) ? null : "Password must contain an uppercase letter, a lowercase letter and a digit.";
    }
}
