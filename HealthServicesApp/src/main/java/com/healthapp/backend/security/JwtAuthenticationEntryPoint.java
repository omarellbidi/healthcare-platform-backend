package com.healthapp.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthapp.backend.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Entry point for handling authentication failures.
 * Returns JSON error response with 401 Unauthorized status when authentication fails.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Called when user attempts to access a protected endpoint without valid authentication.
     * Returns standardized JSON error response instead of default HTML error page.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 status code

        ApiResponse errorResponse = new ApiResponse(
                false,
                "Unauthorized: " + authException.getMessage()
        );

        // Convert error response to JSON and send to client
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}