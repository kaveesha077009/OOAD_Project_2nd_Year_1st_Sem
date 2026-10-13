package com.agroshop.agroshopmangementsystem.config;

import com.agroshop.agroshopmangementsystem.entity.User;
import com.agroshop.agroshopmangementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin_user")) {
            User admin = new User();
            admin.setUsername("admin_user");
            admin.setEmail("admin@agroshop.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setStatus(1);
            userRepository.save(admin);
            System.out.println("Default Admin Account Created: admin_user / admin123");
        }
    }
}