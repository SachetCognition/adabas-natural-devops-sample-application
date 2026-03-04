package com.ntcruise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the NTCruise Java Migration.
 * Migrated from Natural/Adabas NTCRUISE library to Spring Boot 3.x + Java 21 + PostgreSQL.
 */
@SpringBootApplication
public class NtcruiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(NtcruiseApplication.class, args);
    }
}
