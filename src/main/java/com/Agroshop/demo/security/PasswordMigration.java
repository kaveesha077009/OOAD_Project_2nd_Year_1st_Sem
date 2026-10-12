package com.agroshop.demo.security;

import com.agroshop.demo.entity.User;
import com.agroshop.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * The sample data in Agroshop.sql has placeholder passwords such as "hash_admin123".
 * On startup, any password that is not yet a BCrypt hash is converted. The "hash_" prefix is dropped,
 * so the real login password becomes "admin123". Accounts created later should always store a BCrypt hash.
 */
@Component
public class PasswordMigration implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public PasswordMigration(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        for (User u : userRepository.findAll()) {
            String pw = u.getPassword();
            if (!pw.startsWith("$2")) {
                u.setPassword(encoder.encode(pw.startsWith("hash_") ? pw.substring(5) : pw));
                userRepository.save(u);
            }
        }
    }
}
