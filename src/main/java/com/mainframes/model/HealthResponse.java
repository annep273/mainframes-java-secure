package com.mainframes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Health Response model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {
    private String status;
    private String applicationName;
    private String version;
    private LocalDateTime timestamp;
    private String architecture;
    private String operatingSystem;
    private String javaVersion;
    private Long usedMemoryMB;
    private Long maxMemoryMB;
}
