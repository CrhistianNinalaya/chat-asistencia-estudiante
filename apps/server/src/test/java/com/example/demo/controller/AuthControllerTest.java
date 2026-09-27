package com.example.demo.controller;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.dto.AuthDto;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.service.AccountService;
import com.example.demo.enums.AccountType;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 200 OK and auth token when login credentials are valid")
    void login_WithValidCredentials_ReturnsOkAndToken() throws Exception {
        // Arrange
        AuthDto.LoginRequest request = new AuthDto.LoginRequest("estudiante1@demo.com", "password123");
        AuthDto.AccountResponse userResponse = new AuthDto.AccountResponse(
                UUID.randomUUID(),
                "Ana",
                "Garcia",
                "estudiante1@demo.com",
                AccountType.STUDENT
        );
        AuthDto.Response response = new AuthDto.Response("jwt-token-sample", userResponse);
        when(accountService.authenticate(any())).thenReturn(response);

        // Act
        ResultActions result = mockMvc().perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-sample"))
                .andExpect(jsonPath("$.user.email").value("estudiante1@demo.com"))
                .andExpect(jsonPath("$.user.accountType").value("STUDENT"));
        verify(accountService).authenticate(any());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when credentials are bad")
    void login_WithBadCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange
        AuthDto.LoginRequest request = new AuthDto.LoginRequest("estudiante1@demo.com", "wrong-password");
        when(accountService.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        // Act
        ResultActions result = mockMvc().perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email format is invalid")
    void login_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        AuthDto.LoginRequest request = new AuthDto.LoginRequest("not-an-email", "password123");

        // Act
        ResultActions result = mockMvc().perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when required fields are blank")
    void login_WithBlankCredentials_ReturnsBadRequest() throws Exception {
        // Arrange
        AuthDto.LoginRequest request = new AuthDto.LoginRequest("", "");

        // Act
        ResultActions result = mockMvc().perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 204 No Content when logging out")
    void logout_ReturnsNoContent() throws Exception {
        // Act
        ResultActions result = mockMvc().perform(post("/api/auth/logout"));

        // Assert
        result.andExpect(status().isNoContent());
    }
}
