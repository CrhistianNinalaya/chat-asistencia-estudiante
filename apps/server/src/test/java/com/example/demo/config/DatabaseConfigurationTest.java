package com.example.demo.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatabaseConfigurationTest {

    @Test
    @DisplayName("Should verify PostgreSQL driver class is present in classpath")
    void shouldVerifyPostgreSqlDriverPresent() throws ClassNotFoundException {
        // Arrange & Act
        Class<?> driverClass = Class.forName("org.postgresql.Driver");

        // Assert
        assertThat(driverClass).isNotNull();
    }
}
