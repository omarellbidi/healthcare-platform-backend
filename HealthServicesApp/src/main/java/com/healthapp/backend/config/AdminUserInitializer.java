package com.healthapp.backend.config;

import com.healthapp.backend.entity.User;
import com.healthapp.backend.enums.Role;
import com.healthapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes the default admin user on application startup if it doesn't exist.
 *
 * Default Admin Credentials:
 * - Email: admin@healthapp.com
 * - Password: Admin123!
 *
 * IMPORTANT: Change the password immediately after first login in production!
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@healthapp.com";
    private static final String ADMIN_PASSWORD = "Admin123!";

    @Override
    public void run(String... args) {
        // Check if admin user already exists
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            log.info("Admin user already exists: {}", ADMIN_EMAIL);
            return;
        }

        // Create admin user
        User adminUser = new User();
        adminUser.setEmail(ADMIN_EMAIL);
        adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        adminUser.setRole(Role.ADMIN);
        adminUser.setVerified(true);
        adminUser.setSuspended(false);

        userRepository.save(adminUser);

        log.info("========================================");
        log.info("ADMIN USER CREATED SUCCESSFULLY");
        log.info("========================================");
        log.info("Email: {}", ADMIN_EMAIL);
        log.info("Password: {}", ADMIN_PASSWORD);
        log.info("========================================");
        log.warn("SECURITY WARNING: Change this password immediately in production!");
        log.info("========================================");
    }
}
