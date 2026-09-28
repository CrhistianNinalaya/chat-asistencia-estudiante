package com.example.demo.controller;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.example.demo.dto.TicketDto;
import com.example.demo.enums.AccountType;
import com.example.demo.enums.ResolutionCategory;
import com.example.demo.enums.TicketCategory;
import com.example.demo.enums.TicketPriority;
import com.example.demo.enums.TicketStatus;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TicketService ticketService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TicketController ticketController;

    private final UserPrincipal testUser = new UserPrincipal(
            UUID.randomUUID(),
            "Advisor",
            "User",
            "advisor@test.com",
            "password",
            AccountType.ADVISOR,
            Collections.emptyList()
    );

    private MockMvc mockMvc() {
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testUser;
            }
        };

        return MockMvcBuilders.standaloneSetup(ticketController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("Should return 200 OK and broadcast to WebSocket when status is updated")
    void updateStatus_WithValidRequest_ReturnsOkAndBroadcasts() throws Exception {
        // Arrange
        UUID ticketId = UUID.randomUUID();
        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.RESOLVED,
                "Fixed database connection pool leak",
                ResolutionCategory.SYSTEM_FIX
        );

        TicketDto.Response response = new TicketDto.Response(
                ticketId,
                "Database error",
                "Cannot connect to db",
                Instant.now(),
                Instant.now(),
                TicketStatus.RESOLVED,
                "Fixed database connection pool leak",
                ResolutionCategory.SYSTEM_FIX,
                TicketCategory.TECHNICAL,
                TicketPriority.LOW,
                null,
                null
        );

        when(ticketService.updateTicketStatus(eq(ticketId), any(TicketDto.StatusUpdateRequest.class), any()))
                .thenReturn(response);

        // Act
        ResultActions result = mockMvc().perform(patch("/api/tickets/{ticketId}/status", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolutionSummary").value("Fixed database connection pool leak"))
                .andExpect(jsonPath("$.resolutionCategory").value("SYSTEM_FIX"));

        verify(ticketService).updateTicketStatus(eq(ticketId), any(TicketDto.StatusUpdateRequest.class), any());
        verify(messagingTemplate).convertAndSend("/topic/tickets/" + ticketId + "/status", response);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body has null status")
    void updateStatus_WithNullStatus_ReturnsBadRequest() throws Exception {
        // Arrange
        UUID ticketId = UUID.randomUUID();
        String jsonPayload = """
                {
                    "status": null,
                    "resolutionSummary": "No status provided"
                }
                """;

        // Act
        ResultActions result = mockMvc().perform(patch("/api/tickets/{ticketId}/status", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload));

        // Assert
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when service throws AccessDeniedException")
    void updateStatus_WhenAccessDenied_ReturnsForbidden() throws Exception {
        // Arrange
        UUID ticketId = UUID.randomUUID();
        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.IN_PROGRESS,
                null,
                null
        );

        when(ticketService.updateTicketStatus(eq(ticketId), any(), any()))
                .thenThrow(new AccessDeniedException("Advisors cannot modify tickets assigned to another advisor"));

        // Act
        ResultActions result = mockMvc().perform(patch("/api/tickets/{ticketId}/status", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Advisors cannot modify tickets assigned to another advisor"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when ticket does not exist")
    void updateStatus_WhenTicketNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        UUID ticketId = UUID.randomUUID();
        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.IN_PROGRESS,
                null,
                null
        );

        when(ticketService.updateTicketStatus(eq(ticketId), any(), any()))
                .thenThrow(new EntityNotFoundException("Ticket not found with id: " + ticketId));

        // Act
        ResultActions result = mockMvc().perform(patch("/api/tickets/{ticketId}/status", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Ticket not found with id: " + ticketId));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when service throws IllegalArgumentException")
    void updateStatus_WhenServiceThrowsIllegalArgument_ReturnsBadRequest() throws Exception {
        // Arrange
        UUID ticketId = UUID.randomUUID();
        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.RESOLVED,
                null,
                null
        );

        when(ticketService.updateTicketStatus(eq(ticketId), any(), any()))
                .thenThrow(new IllegalArgumentException("Resolution summary is required when resolving a ticket"));

        // Act
        ResultActions result = mockMvc().perform(patch("/api/tickets/{ticketId}/status", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Resolution summary is required when resolving a ticket"));
    }

    @Test
    @DisplayName("Should return 409 Conflict when service throws IllegalStateException")
    void updateStatus_WhenTicketIsClosed_ReturnsConflict() throws Exception {
        // Arrange
        UUID ticketId = UUID.randomUUID();
        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.IN_PROGRESS,
                null,
                null
        );

        when(ticketService.updateTicketStatus(eq(ticketId), any(), any()))
                .thenThrow(new IllegalStateException("Cannot change the status of a closed ticket"));

        // Act
        ResultActions result = mockMvc().perform(patch("/api/tickets/{ticketId}/status", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cannot change the status of a closed ticket"));
    }

    @Test
    @DisplayName("Should return 200 OK when querying list of tickets with status filter")
    void listAll_WithStatusParam_ReturnsList() throws Exception {
        // Arrange
        TicketDto.Response response = new TicketDto.Response(
                UUID.randomUUID(),
                "Title",
                "Description",
                Instant.now(),
                null,
                TicketStatus.IN_PROGRESS,
                null,
                null,
                TicketCategory.GENERAL,
                TicketPriority.LOW,
                null,
                null
        );

        when(ticketService.listTickets(eq(TicketStatus.IN_PROGRESS), any(), any()))
                .thenReturn(List.of(response));

        // Act
        ResultActions result = mockMvc().perform(get("/api/tickets")
                .param("status", "IN_PROGRESS"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
        verify(ticketService).listTickets(eq(TicketStatus.IN_PROGRESS), any(), any());
    }

    @Test
    @DisplayName("Should return 201 Created when student creates ticket")
    void create_WithValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        TicketDto.CreateRequest request = new TicketDto.CreateRequest("Title", "Description", TicketCategory.GENERAL);
        TicketDto.Response response = new TicketDto.Response(
                UUID.randomUUID(),
                "Title",
                "Description",
                Instant.now(),
                null,
                TicketStatus.OPEN,
                null,
                null,
                TicketCategory.GENERAL,
                TicketPriority.LOW,
                null,
                null
        );

        when(ticketService.createTicket(any(), any())).thenReturn(response);

        // Act
        ResultActions result = mockMvc().perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.title").value("Title"));
    }
}
