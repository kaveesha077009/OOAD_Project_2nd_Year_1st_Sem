package com.agroshop.agroshopmangementsystem.service;

import com.agroshop.agroshopmangementsystem.dto.LoginDTO;
import com.agroshop.agroshopmangementsystem.entity.User;
import com.agroshop.agroshopmangementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // Temporary in-memory storage for OTPs (Key: email in lowercase, Value: OTP string)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public Map<String, Object> login(LoginDTO loginDTO) {
        Map<String, Object> response = new HashMap<>();

        // 1. Fetch user by username
        Optional<User> userOpt = userRepository.findByUsername(loginDTO.getUsername());

        if (userOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Access Denied! Invalid credentials.");
            return response;
        }

        User user = userOpt.get();
        String dbPassword = user.getPassword();
        String inputPassword = loginDTO.getPassword();

        boolean passwordMatches = false;

        // 2. Hybrid Password Verification
        if (dbPassword != null && (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$") || dbPassword.startsWith("$2y$"))) {
            // Evaluates BCrypt hashed passwords (from UI / BCrypt Insert)
            passwordMatches = passwordEncoder.matches(inputPassword, dbPassword);
        } else {
            // Evaluates Plain Text passwords (from Direct SQL Insert)
            passwordMatches = inputPassword.equals(dbPassword);
        }

        if (!passwordMatches) {
            response.put("status", "error");
            response.put("message", "Access Denied! Invalid credentials.");
            return response;
        }

        // 3. Role Validation (Case-Insensitive)
        String inputRole = loginDTO.getRole() != null ? loginDTO.getRole().trim() : "";
        String dbRole = user.getRole() != null ? user.getRole().trim() : "";

        if (!inputRole.isEmpty() && !dbRole.equalsIgnoreCase(inputRole)) {
            response.put("status", "error");
            response.put("message", "Access Denied! Role mismatch.");
            return response;
        }

        // 4. Successful Login Response
        response.put("status", "success");
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        return response;
    }

    public Map<String, Object> sendOtp(String email) {
        Map<String, Object> response = new HashMap<>();

        if (email == null || email.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Please enter a valid email address!");
            return response;
        }

        String normalizedEmail = email.trim().toLowerCase();

        // Validate user existence by checking all users
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().trim().equalsIgnoreCase(normalizedEmail))
                .findFirst();

        if (userOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "No account registered with email: " + email);
            return response;
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);
        otpStorage.put(normalizedEmail, otp);

        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(normalizedEmail);
                message.setSubject("AgroShop Account Recovery OTP");
                message.setText("Your OTP code for password reset is: " + otp);
                mailSender.send(message);
            }
            response.put("status", "success");
            response.put("message", "OTP sent successfully to " + email);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback for local development if mail sender is offline
            response.put("status", "success");
            response.put("message", "OTP generated: " + otp);
        }

        return response;
    }

    public Map<String, Object> resetPasswordWithOtp(String email, String otp, String newPassword) {
        Map<String, Object> response = new HashMap<>();

        if (email == null || otp == null || newPassword == null || email.trim().isEmpty() || otp.trim().isEmpty() || newPassword.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "All fields are required!");
            return response;
        }

        String normalizedEmail = email.trim().toLowerCase();
        String savedOtp = otpStorage.get(normalizedEmail);

        if (savedOtp == null || !savedOtp.equals(otp.trim())) {
            response.put("status", "error");
            response.put("message", "Invalid or expired OTP code!");
            return response;
        }

        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().trim().equalsIgnoreCase(normalizedEmail))
                .findFirst();

        if (userOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "User not found!");
            return response;
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);

        // Invalidate OTP after successful reset
        otpStorage.remove(normalizedEmail);

        response.put("status", "success");
        response.put("message", "Password reset successfully! Please login with your new password.");
        return response;
    }
}