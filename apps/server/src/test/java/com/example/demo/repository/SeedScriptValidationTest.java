package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SeedScriptValidationTest {

    @Test
    @DisplayName("Should verify seed-postgres.sql exists and contains valid PostgreSQL UUID statements")
    void shouldVerifySeedScriptExists() throws IOException {
        // Arrange
        Path seedPath = Path.of("../../scripts/seed-postgres.sql");
        if (!Files.exists(seedPath)) {
            seedPath = Path.of("scripts/seed-postgres.sql");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Act
        boolean exists = Files.exists(seedPath);

        // Assert
        assertThat(exists).isTrue();
        String content = Files.readString(seedPath);
        assertThat(content).contains("INSERT INTO accounts");
        assertThat(content).contains("INSERT INTO students");
        assertThat(content).contains("INSERT INTO advisors");
        assertThat(content).contains("INSERT INTO admins");
        assertThat(content).contains("INSERT INTO tickets");
        assertThat(content).contains("INSERT INTO ticket_assignment_history");
        assertThat(content).contains("INSERT INTO ticket_activities");
        assertThat(content).contains("INSERT INTO messages");
        assertThat(content).contains("'TICKET_CREATED'");
        assertThat(content).contains("'ADVISOR_ASSIGNED'");
        assertThat(content).doesNotContain("UUID_TO_BIN");

        String passwordHash = "$2a$10$PrQKXhy5QCBBqjHSbnRICuCAuQ4fm4aZM2E/K5LBhJFIwj6e0DY9K";
        assertThat(content).contains(passwordHash);
        assertThat(encoder.matches("password123", passwordHash)).isTrue();
    }
}
