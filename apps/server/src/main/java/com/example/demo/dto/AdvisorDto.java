package com.example.demo.dto;

import java.io.Serializable;
import java.util.UUID;

import com.example.demo.entity.AdvisorEntity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class AdvisorDto {

    private AdvisorDto() {}

    public record CreateRequest(
            @NotBlank(message = "First name is required")
            @Size(max = 50, message = "First name must not exceed 50 characters")
            String firstName,

            @NotBlank(message = "Last name is required")
            @Size(max = 50, message = "Last name must not exceed 50 characters")
            String lastName,

            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            @Size(max = 100, message = "Email must not exceed 100 characters")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
            String password,

            @NotBlank(message = "Employee code is required")
            @Size(max = 30, message = "Employee code must not exceed 30 characters")
            String employeeCode,

            @Positive(message = "Max concurrent tickets must be positive")
            Integer maxConcurrentTickets
    ) {}

    public record AvailabilityRequest(
            @NotNull(message = "Availability status is required")
            Boolean isAvailable
    ) {}

    public record Response(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String employeeCode,
            Integer maxConcurrentTickets,
            Boolean isAvailable
    ) implements Serializable {

        private static final long serialVersionUID = 1L;

        public static Response fromEntity(AdvisorEntity entity) {
            return new Response(
                    entity.getId(),
                    entity.getFirstName(),
                    entity.getLastName(),
                    entity.getEmail(),
                    entity.getEmployeeCode(),
                    entity.getMaxConcurrentTickets(),
                    entity.getIsAvailable()
            );
        }
    }
}
