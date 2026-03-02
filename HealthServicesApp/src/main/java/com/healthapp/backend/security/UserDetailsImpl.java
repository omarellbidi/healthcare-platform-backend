package com.healthapp.backend.security;

import com.healthapp.backend.entity.User;
import com.healthapp.backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Spring Security UserDetails implementation for authentication and authorization.
 * Wraps User entity and provides role-based authorities for access control.
 */
@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private UUID id;
    private String email; // Used as username in Spring Security
    private String password; // bcrypt hashed password
    private Role role; // PATIENT, DOCTOR, or ADMIN
    private Boolean verified; // Controls isEnabled() - unverified users cannot login
    private Collection<? extends GrantedAuthority> authorities; // Spring Security authorities

    /**
     * Factory method to build UserDetailsImpl from User entity.
     * Converts Role enum to Spring Security GrantedAuthority with "ROLE_" prefix.
     */
    public static UserDetailsImpl build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getVerified(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return verified; // Only verified users can login - enforces email verification requirement
    }
}