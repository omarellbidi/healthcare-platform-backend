package com.healthapp.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for audit log responses.
 */
@Data
public class AuditLogDTO {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String action;
    private String resourceType;
    private UUID resourceId;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;
}
