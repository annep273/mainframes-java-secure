package com.mainframes.controller;

import com.mainframes.model.HealthResponse;
import com.mainframes.service.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Controller for application health checks
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    private final HealthService healthService;

    @GetMapping("/health")
    @Operation(summary = "Get application health status", 
               description = "Returns the health status of the application including system information")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(healthService.getHealth());
    }
}
