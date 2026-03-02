package com.healthapp.backend.service;

import com.healthapp.backend.constants.AppConstants;
import com.healthapp.backend.dto.AuditLogDTO;
import com.healthapp.backend.entity.AuditLog;
import com.healthapp.backend.entity.User;
import com.healthapp.backend.repository.AuditLogRepository;
import com.healthapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing audit logs.
 * Tracks critical system actions for security and compliance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Create an audit log entry.
     */
    @Transactional
    public void log(UUID userId, String action, String resourceType, UUID resourceId, String details, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog();

            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                auditLog.setUser(user);
            }

            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setDetails(details);
            auditLog.setIpAddress(ipAddress);

            auditLogRepository.save(auditLog);
            log.info("Audit log created: action={}, resourceType={}, resourceId={}", action, resourceType, resourceId);
        } catch (Exception e) {
            // Don't let audit logging failures break the application
            log.error("Failed to create audit log: action={}, error={}", action, e.getMessage());
        }
    }

    /**
     * Simplified log method without IP address.
     */
    @Transactional
    public void log(UUID userId, String action, String resourceType, UUID resourceId, String details) {
        log(userId, action, resourceType, resourceId, details, null);
    }

    /**
     * Get audit logs with filters.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogs(
            UUID userId,
            String action,
            String resourceType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogRepository.findWithFilters(
                userId, action, resourceType, startDate, endDate, pageable
        );

        return auditLogs.map(this::mapToDTO);
    }

    /**
     * Get recent activity count (last 24 hours).
     */
    @Transactional(readOnly = true)
    public long getRecentActivityCount() {
        LocalDateTime since = LocalDateTime.now().minusHours(AppConstants.FIRST_REMINDER_HOURS);
        return auditLogRepository.countRecentActions(since);
    }

    /**
     * Map AuditLog entity to DTO.
     */
    private AuditLogDTO mapToDTO(AuditLog auditLog) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(auditLog.getId());

        if (auditLog.getUser() != null) {
            dto.setUserId(auditLog.getUser().getId());
            dto.setUserEmail(auditLog.getUser().getEmail());
        }

        dto.setAction(auditLog.getAction());
        dto.setResourceType(auditLog.getResourceType());
        dto.setResourceId(auditLog.getResourceId());
        dto.setDetails(auditLog.getDetails());
        dto.setIpAddress(auditLog.getIpAddress());
        dto.setCreatedAt(auditLog.getCreatedAt());

        return dto;
    }
}
