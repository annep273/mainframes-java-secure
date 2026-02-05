package com.mainframes.controller;

import com.mainframes.model.HealthResponse;
import com.mainframes.service.HealthService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthService healthService;

    @MockBean
    private Bucket rateLimitBucket;

    @BeforeEach
    void setUp() {
        // Mock the rate limiter to always allow requests in tests
        when(rateLimitBucket.tryConsume(1)).thenReturn(true);
    }

    @Test
    void testHealthEndpoint() throws Exception {
        HealthResponse healthResponse = HealthResponse.builder()
                .status("UP")
                .applicationName("mainframes-java-app")
                .version("1.0.0")
                .timestamp(LocalDateTime.now())
                .architecture("s390x")
                .operatingSystem("Linux")
                .javaVersion("21")
                .usedMemoryMB(100L)
                .maxMemoryMB(512L)
                .build();

        when(healthService.getHealth()).thenReturn(healthResponse);

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.applicationName").value("mainframes-java-app"));
    }
}
