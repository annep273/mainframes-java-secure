package com.mainframes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application class for Mainframes Java Application
 * Designed for deployment on s390x OpenShift cluster
 */
@SpringBootApplication
public class MainframesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainframesApplication.class, args);
    }
}
