package com.healthapp.backend.repository;

import com.healthapp.backend.entity.User;
import com.healthapp.backend.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByResetToken(String token);
    boolean existsByEmail(String email);

    // Admin feature methods
    long countByRole(Role role);
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}