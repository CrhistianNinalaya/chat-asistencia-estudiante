package com.example.demo.dto;

import java.io.Serializable;
import java.util.UUID;

import com.example.demo.entity.StudentEntity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class StudentDto {

    private StudentDto() {}

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

            @NotBlank(message = "Student code is required")
            @Size(max = 30, message = "Student code must not exceed 30 characters")
            String studentCode,

            @NotBlank(message = "Career is required")
            @Size(max = 100, message = "Career must not exceed 100 characters")
            String career,

            @NotNull(message = "Current semester is required")
            @Min(value = 1, message = "Current semester must be at least 1")
            @Max(value = 15, message = "Current semester must not exceed 15")
            Integer currentSemester,

            @Size(max = 20, message = "Phone number must not exceed 20 characters")
            String phoneNumber
    ) {}

    public record Response(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String studentCode,
            String career,
            Integer currentSemester,
            String phoneNumber
    ) implements Serializable {

        private static final long serialVersionUID = 1L;

        public static Response fromEntity(StudentEntity entity) {
            return new Response(
                    entity.getId(),
                    entity.getFirstName(),
                    entity.getLastName(),
                    entity.getEmail(),
                    entity.getStudentCode(),
                    entity.getCareer(),
                    entity.getCurrentSemester(),
                    entity.getPhoneNumber()
            );
        }
    }
}
