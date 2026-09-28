package com.example.demo.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.demo.dto.TicketDto;
import com.example.demo.entity.AdvisorEntity;
import com.example.demo.entity.StudentEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.enums.AccountType;
import com.example.demo.enums.ResolutionCategory;
import com.example.demo.enums.TicketCategory;
import com.example.demo.enums.TicketStatus;
import com.example.demo.repository.TicketRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.impl.TicketServiceImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private UserPrincipal createUser(UUID id, AccountType accountType) {
        return new UserPrincipal(
                id,
                "Test",
                "User",
                "test@test.com",
                "pass",
                accountType,
                Collections.emptyList()
        );
    }

    @Test
    @DisplayName("Should create ticket with OPEN status when requested by a student")
    void createTicket_WithStudentRole_CreatesTicketWithOpenStatus() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UserPrincipal studentUser = createUser(studentId, AccountType.STUDENT);
        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setId(studentId);

        TicketDto.CreateRequest request = new TicketDto.CreateRequest("Title", "Description", TicketCategory.GENERAL);

        when(entityManager.getReference(StudentEntity.class, studentId)).thenReturn(studentEntity);
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> {
            TicketEntity saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // Act
        TicketDto.Response response = ticketService.createTicket(request, studentUser);

        // Assert
        assertNotNull(response);
        assertEquals(TicketStatus.OPEN, response.status());
        assertEquals("Title", response.title());
        verify(ticketRepository).save(any(TicketEntity.class));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when non-student tries to create ticket")
    void createTicket_WithNonStudent_ThrowsAccessDeniedException() {
        // Arrange
        UserPrincipal advisorUser = createUser(UUID.randomUUID(), AccountType.ADVISOR);
        TicketDto.CreateRequest request = new TicketDto.CreateRequest("Title", "Desc", TicketCategory.TECHNICAL);

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> ticketService.createTicket(request, advisorUser)
        );
        assertEquals("Only students can create tickets", exception.getMessage());
    }

    @Test
    @DisplayName("Should transition to RESOLVED and set resolution metadata when valid")
    void updateTicketStatus_ToResolved_SetsMetadataAndClosedAt() {
        // Arrange
        UUID advisorId = UUID.randomUUID();
        UserPrincipal advisorUser = createUser(advisorId, AccountType.ADVISOR);
        AdvisorEntity advisor = new AdvisorEntity();
        advisor.setId(advisorId);

        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setAssignedTo(advisor);

        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.RESOLVED,
                "Applied bug fix patch",
                ResolutionCategory.SYSTEM_FIX
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TicketDto.Response response = ticketService.updateTicketStatus(ticketId, request, advisorUser);

        // Assert
        assertNotNull(response);
        assertEquals(TicketStatus.RESOLVED, response.status());
        assertEquals("Applied bug fix patch", response.resolutionSummary());
        assertEquals(ResolutionCategory.SYSTEM_FIX, response.resolutionCategory());
        assertNotNull(response.closedAt());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when resolving without resolution summary")
    void updateTicketStatus_ToResolvedWithoutSummary_ThrowsIllegalArgumentException() {
        // Arrange
        UUID advisorId = UUID.randomUUID();
        UserPrincipal advisorUser = createUser(advisorId, AccountType.ADVISOR);
        AdvisorEntity advisor = new AdvisorEntity();
        advisor.setId(advisorId);

        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setAssignedTo(advisor);

        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.RESOLVED,
                "",
                ResolutionCategory.SYSTEM_FIX
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.updateTicketStatus(ticketId, request, advisorUser)
        );
        assertEquals("Resolution summary is required when resolving a ticket", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when resolving without resolution category")
    void updateTicketStatus_ToResolvedWithoutCategory_ThrowsIllegalArgumentException() {
        // Arrange
        UUID advisorId = UUID.randomUUID();
        UserPrincipal advisorUser = createUser(advisorId, AccountType.ADVISOR);
        AdvisorEntity advisor = new AdvisorEntity();
        advisor.setId(advisorId);

        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setAssignedTo(advisor);

        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.RESOLVED,
                "Fixed the issue",
                null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.updateTicketStatus(ticketId, request, advisorUser)
        );
        assertEquals("Resolution category is required when resolving a ticket", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when changing status of an already CLOSED ticket")
    void updateTicketStatus_WhenAlreadyClosed_ThrowsIllegalStateException() {
        // Arrange
        UUID advisorId = UUID.randomUUID();
        UserPrincipal advisorUser = createUser(advisorId, AccountType.ADVISOR);
        AdvisorEntity advisor = new AdvisorEntity();
        advisor.setId(advisorId);

        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setAssignedTo(advisor);

        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.IN_PROGRESS,
                null,
                null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.updateTicketStatus(ticketId, request, advisorUser)
        );
        assertEquals("Cannot change the status of a closed ticket", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when advisor modifies ticket assigned to someone else")
    void updateTicketStatus_WhenAssignedToAnotherAdvisor_ThrowsAccessDeniedException() {
        // Arrange
        UUID advisorId = UUID.randomUUID();
        UserPrincipal advisorUser = createUser(advisorId, AccountType.ADVISOR);

        AdvisorEntity otherAdvisor = new AdvisorEntity();
        otherAdvisor.setId(UUID.randomUUID());

        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setAssignedTo(otherAdvisor);

        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.IN_PROGRESS,
                null,
                null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> ticketService.updateTicketStatus(ticketId, request, advisorUser)
        );
        assertEquals("Advisors cannot modify tickets assigned to another advisor", exception.getMessage());
    }

    @Test
    @DisplayName("Should assign advisor automatically when ticket was unassigned")
    void updateTicketStatus_WhenUnassigned_AssignsCurrentAdvisor() {
        // Arrange
        UUID advisorId = UUID.randomUUID();
        UserPrincipal advisorUser = createUser(advisorId, AccountType.ADVISOR);
        AdvisorEntity advisor = new AdvisorEntity();
        advisor.setId(advisorId);

        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setAssignedTo(null);

        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.IN_PROGRESS,
                null,
                null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(entityManager.getReference(AdvisorEntity.class, advisorId)).thenReturn(advisor);
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TicketDto.Response response = ticketService.updateTicketStatus(ticketId, request, advisorUser);

        // Assert
        assertNotNull(response);
        assertEquals(TicketStatus.IN_PROGRESS, response.status());
        verify(entityManager).getReference(AdvisorEntity.class, advisorId);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when ticket does not exist")
    void updateTicketStatus_WhenTicketNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        UUID advisorId = UUID.randomUUID();
        UserPrincipal advisorUser = createUser(advisorId, AccountType.ADVISOR);
        UUID nonExistentTicketId = UUID.randomUUID();

        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.IN_PROGRESS,
                null,
                null
        );

        when(ticketRepository.findById(nonExistentTicketId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> ticketService.updateTicketStatus(nonExistentTicketId, request, advisorUser)
        );
        assertEquals("Ticket not found with id: " + nonExistentTicketId, exception.getMessage());
    }

    @Test
    @DisplayName("Should allow admin to update ticket status without reassigning advisor")
    void updateTicketStatus_WhenAdmin_AllowsUpdateWithoutReassigning() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        UserPrincipal adminUser = createUser(adminId, AccountType.ADMIN);

        UUID advisorId = UUID.randomUUID();
        AdvisorEntity advisor = new AdvisorEntity();
        advisor.setId(advisorId);

        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setAssignedTo(advisor);

        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.RESOLVED,
                "Resolved by administrator",
                ResolutionCategory.SYSTEM_FIX
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TicketDto.Response response = ticketService.updateTicketStatus(ticketId, request, adminUser);

        // Assert
        assertNotNull(response);
        assertEquals(TicketStatus.RESOLVED, response.status());
        assertEquals("Resolved by administrator", response.resolutionSummary());
        assertEquals(ResolutionCategory.SYSTEM_FIX, response.resolutionCategory());
        assertEquals(advisorId, ticket.getAssignedTo().getId());
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when student tries to update ticket status")
    void updateTicketStatus_WhenStudent_ThrowsAccessDeniedException() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UserPrincipal studentUser = createUser(studentId, AccountType.STUDENT);

        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.OPEN);

        TicketDto.StatusUpdateRequest request = new TicketDto.StatusUpdateRequest(
                TicketStatus.CLOSED,
                null,
                null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> ticketService.updateTicketStatus(ticketId, request, studentUser)
        );
        assertEquals("Only advisors and administrators can update ticket status", exception.getMessage());
    }

    @Test
    @DisplayName("Should filter tickets by status for student")
    void listTickets_ForStudent_WithStatusFilter() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UserPrincipal studentUser = createUser(studentId, AccountType.STUDENT);

        TicketEntity ticket = new TicketEntity();
        ticket.setId(UUID.randomUUID());
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setStartedAt(Instant.now());

        when(ticketRepository.findAllByStudentAndStatus(studentId, TicketStatus.RESOLVED)).thenReturn(List.of(ticket));

        // Act
        List<TicketDto.Response> results = ticketService.listTickets(TicketStatus.RESOLVED, null, studentUser);

        // Assert
        assertEquals(1, results.size());
        assertEquals(TicketStatus.RESOLVED, results.getFirst().status());
    }

    @Test
    @DisplayName("Should query assigned or unassigned tickets when advisor filters by status")
    void listTickets_ForAdvisor_WithStatusFilter_QueriesAssignedOrUnassignedTickets() {
        // Arrange
        UUID advisorId = UUID.randomUUID();
        UserPrincipal advisorUser = createUser(advisorId, AccountType.ADVISOR);

        TicketEntity ticket = new TicketEntity();
        ticket.setId(UUID.randomUUID());
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setStartedAt(Instant.now());

        when(ticketRepository.findAssignedOrUnassignedByStatus(advisorId, TicketStatus.IN_PROGRESS))
                .thenReturn(List.of(ticket));

        // Act
        List<TicketDto.Response> results = ticketService.listTickets(TicketStatus.IN_PROGRESS, null, advisorUser);

        // Assert
        assertEquals(1, results.size());
        assertEquals(TicketStatus.IN_PROGRESS, results.getFirst().status());
        verify(ticketRepository).findAssignedOrUnassignedByStatus(advisorId, TicketStatus.IN_PROGRESS);
    }
}
