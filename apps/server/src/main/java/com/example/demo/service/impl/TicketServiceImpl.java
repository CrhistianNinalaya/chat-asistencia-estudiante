package com.example.demo.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.TicketDto;
import com.example.demo.entity.AdvisorEntity;
import com.example.demo.entity.StudentEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.enums.AccountType;
import com.example.demo.enums.TicketPriority;
import com.example.demo.enums.TicketStatus;
import com.example.demo.repository.TicketRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.TicketService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto.Response> listTickets(TicketStatus status, TicketPriority priority, UserPrincipal user) {
        if (user == null) {
            return Collections.emptyList();
        }

        Instant now = Instant.now();
        Instant lowPriorityThreshold = now.minus(TicketEntity.LOW_PRIORITY_MAX_DAYS, ChronoUnit.DAYS);
        Instant mediumPriorityThreshold = now.minus(TicketEntity.MEDIUM_PRIORITY_MAX_DAYS, ChronoUnit.DAYS);

        List<TicketEntity> entities = switch (user.getAccountType()) {
            case ADVISOR ->
                listTicketsForAdvisor(user.getId(), status, priority, lowPriorityThreshold, mediumPriorityThreshold);
            case STUDENT ->
                listTicketsForStudent(user.getId(), status, priority);
            case ADMIN ->
                listTicketsForAdmin(status, priority);
            case null ->
                Collections.emptyList();
        };

        return entities.stream()
                .map(TicketDto.Response::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public TicketDto.Response createTicket(TicketDto.CreateRequest request, UserPrincipal user) {
        if (user == null || user.getAccountType() != AccountType.STUDENT) {
            throw new AccessDeniedException("Only students can create tickets");
        }

        StudentEntity student = entityManager.getReference(StudentEntity.class, user.getId());

        TicketEntity ticket = new TicketEntity();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setCategory(request.category());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setStartedAt(Instant.now());
        ticket.setGeneratedBy(student);

        TicketEntity saved = ticketRepository.save(ticket);
        return TicketDto.Response.fromEntity(saved);
    }

    @Override
    @Transactional
    public TicketDto.Response updateTicketStatus(UUID ticketId, TicketDto.StatusUpdateRequest request, UserPrincipal user) {
        validateStatusUpdateRequest(request);
        TicketEntity ticket = getTicketOrThrow(ticketId);
        validateStatusUpdateAuthorization(user, ticket);
        applyStatusTransition(ticket, request);

        TicketEntity saved = ticketRepository.save(ticket);
        return TicketDto.Response.fromEntity(saved);
    }

    private void validateStatusUpdateRequest(TicketDto.StatusUpdateRequest request) {
        if (request == null || request.status() == null) {
            throw new IllegalArgumentException("Target status is required");
        }
    }

    private TicketEntity getTicketOrThrow(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));
    }

    private void validateStatusUpdateAuthorization(UserPrincipal user, TicketEntity ticket) {
        if (user == null || (user.getAccountType() != AccountType.ADVISOR && user.getAccountType() != AccountType.ADMIN)) {
            throw new AccessDeniedException("Only advisors and administrators can update ticket status");
        }
        if (user.getAccountType() == AccountType.ADMIN) {
            // Intentionally empty: admins have unrestricted access to update any ticket status
            return;
        }
        if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().getId().equals(user.getId())) {
            throw new AccessDeniedException("Advisors cannot modify tickets assigned to another advisor");
        }
        if (ticket.getAssignedTo() == null) {
            AdvisorEntity advisor = entityManager.getReference(AdvisorEntity.class, user.getId());
            ticket.setAssignedTo(advisor);
        }
    }

    private void applyStatusTransition(TicketEntity ticket, TicketDto.StatusUpdateRequest request) {
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new IllegalStateException("Cannot change the status of a closed ticket");
        }
        switch (request.status()) {
            case RESOLVED ->
                applyResolvedStatus(ticket, request);
            case CLOSED ->
                applyClosedStatus(ticket, request);
            case IN_PROGRESS, WAITING_STUDENT, ASSIGNED, OPEN ->
                applyActiveStatus(ticket, request.status());
        }
    }

    private void applyResolvedStatus(TicketEntity ticket, TicketDto.StatusUpdateRequest request) {
        if (request.resolutionSummary() == null || request.resolutionSummary().isBlank()) {
            throw new IllegalArgumentException("Resolution summary is required when resolving a ticket");
        }
        if (request.resolutionCategory() == null) {
            throw new IllegalArgumentException("Resolution category is required when resolving a ticket");
        }
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolutionSummary(request.resolutionSummary().trim());
        ticket.setResolutionCategory(request.resolutionCategory());
        ticket.setClosedAt(Instant.now());
    }

    private void applyClosedStatus(TicketEntity ticket, TicketDto.StatusUpdateRequest request) {
        ticket.setStatus(TicketStatus.CLOSED);
        if (request.resolutionSummary() != null && !request.resolutionSummary().isBlank()) {
            ticket.setResolutionSummary(request.resolutionSummary().trim());
        }
        if (request.resolutionCategory() != null) {
            ticket.setResolutionCategory(request.resolutionCategory());
        }
        ticket.setClosedAt(Instant.now());
    }

    private void applyActiveStatus(TicketEntity ticket, TicketStatus targetStatus) {
        ticket.setStatus(targetStatus);
        ticket.setClosedAt(null);
    }

    private List<TicketEntity> listTicketsForAdvisor(
            UUID advisorId, TicketStatus status, TicketPriority priority, Instant lowPriorityThreshold, Instant mediumPriorityThreshold) {
        if (status != null) {
            List<TicketEntity> tickets = ticketRepository.findAssignedOrUnassignedByStatus(advisorId, status);
            return filterByPriority(tickets, priority);
        }
        return listActiveTicketsForAdvisor(advisorId, priority, lowPriorityThreshold, mediumPriorityThreshold);
    }

    private List<TicketEntity> listActiveTicketsForAdvisor(
            UUID advisorId, TicketPriority priority, Instant lowPriorityThreshold, Instant mediumPriorityThreshold) {
        if (priority == null) {
            return ticketRepository.findActiveForAdvisor(advisorId);
        }
        return switch (priority) {
            case LOW ->
                ticketRepository.findActiveForAdvisorStartedAfter(advisorId, lowPriorityThreshold);
            case MEDIUM ->
                ticketRepository.findActiveForAdvisorStartedBetween(advisorId, lowPriorityThreshold, mediumPriorityThreshold);
            case HIGH ->
                ticketRepository.findActiveForAdvisorStartedBeforeOrEqual(advisorId, mediumPriorityThreshold);
        };
    }

    private List<TicketEntity> listTicketsForStudent(UUID studentId, TicketStatus status, TicketPriority priority) {
        List<TicketEntity> tickets = status == null
                ? ticketRepository.findAllByStudent(studentId)
                : ticketRepository.findAllByStudentAndStatus(studentId, status);
        return filterByPriority(tickets, priority);
    }

    private List<TicketEntity> listTicketsForAdmin(TicketStatus status, TicketPriority priority) {
        List<TicketEntity> tickets = ticketRepository.findAllForAdmin(status);
        return filterByPriority(tickets, priority);
    }

    private List<TicketEntity> filterByPriority(List<TicketEntity> tickets, TicketPriority priority) {
        if (priority == null) {
            return tickets;
        }
        return tickets.stream()
                .filter(t -> t.getPriority() == priority)
                .toList();
    }
}
