package com.mainframes.service;

import com.mainframes.model.HealthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.LocalDateTime;

/**
 * Service for health check operations
 */
@Service
public class HealthService {

    @Value("${spring.application.name:mainframes-java-app}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String version;

    public HealthResponse getHealth() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);

        return HealthResponse.builder()
                .status("UP")
                .applicationName(applicationName)
                .version(version)
                .timestamp(LocalDateTime.now())
                .architecture(System.getProperty("os.arch"))
                .operatingSystem(System.getProperty("os.name"))
                .javaVersion(System.getProperty("java.version"))
                .usedMemoryMB(usedMemory)
                .maxMemoryMB(maxMemory)
                .build();
    }
}
