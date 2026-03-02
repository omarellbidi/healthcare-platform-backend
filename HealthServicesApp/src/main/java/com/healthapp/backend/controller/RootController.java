package com.healthapp.backend.controller;

import com.healthapp.backend.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Root controller for API welcome endpoint.
 * Provides basic information about the HealthApp API.
 */
@RestController
@Tag(name = "Root", description = "API root endpoint")
public class RootController {

    @GetMapping("/")
    @Operation(summary = "API Welcome", description = "Returns API information and status")
    public ResponseEntity<ApiResponse> root() {
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Welcome to HealthApp API v1.0.0 - Visit /swagger-ui/index.html for documentation"
        ));
    }
}
