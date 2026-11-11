package com.healthapp.backend.config;

import com.healthapp.backend.entity.User;
import com.healthapp.backend.enums.Role;
import com.healthapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the initial admin user on application startup if it doesn't exist.
 * The credentials are read from the ADMIN_EMAIL and ADMIN_PASSWORD environment variables.
 * If they are not set, no admin user is created.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.warn("ADMIN_EMAIL or ADMIN_PASSWORD not set, skipping admin user creation");
            return;
        }

        // Check if admin user already exists
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin user already exists");
            return;
        }

        // Create admin user
        User adminUser = new User();
        adminUser.setEmail(adminEmail);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setRole(Role.ADMIN);
        adminUser.setVerified(true);
        adminUser.setSuspended(false);

        userRepository.save(adminUser);

        log.info("Admin user created: {}", adminEmail);
    }
}
