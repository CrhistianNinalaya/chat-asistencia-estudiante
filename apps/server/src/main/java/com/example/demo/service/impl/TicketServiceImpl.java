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
import com.example.demo.entity.StudentEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.repository.TicketRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.TicketService;
import com.example.demo.enums.AccountType;
import com.example.demo.enums.TicketPriority;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto.Response> listTickets(Boolean active, TicketPriority priority, UserPrincipal user) {
        if (user == null) {
            return Collections.emptyList();
        }

        Instant now = Instant.now();
        Instant lowPriorityThreshold = now.minus(TicketEntity.LOW_PRIORITY_MAX_DAYS, ChronoUnit.DAYS);
        Instant mediumPriorityThreshold = now.minus(TicketEntity.MEDIUM_PRIORITY_MAX_DAYS, ChronoUnit.DAYS);

        List<TicketEntity> entities = switch (user.getAccountType()) {
            case ADVISOR -> listTicketsForAdvisor(user.getId(), active, priority, lowPriorityThreshold, mediumPriorityThreshold);
            case STUDENT -> listTicketsForStudent(user.getId(), active, priority);
            case ADMIN -> ticketRepository.findAllForAdmin(active, priority);
            case null -> Collections.emptyList();
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
        ticket.setActive(true);
        ticket.setStartedAt(Instant.now());
        ticket.setGeneratedBy(student);

        TicketEntity saved = ticketRepository.save(ticket);
        return TicketDto.Response.fromEntity(saved);
    }

    private List<TicketEntity> listTicketsForAdvisor(
            UUID advisorId, Boolean active, TicketPriority priority, Instant lowPriorityThreshold, Instant mediumPriorityThreshold) {
        boolean isActive = active == null || Boolean.TRUE.equals(active);
        if (isActive) {
            return listActiveTicketsForAdvisor(advisorId, priority, lowPriorityThreshold, mediumPriorityThreshold);
        }
        return listClosedTicketsForAdvisor(advisorId, priority);
    }

    private List<TicketEntity> listActiveTicketsForAdvisor(
            UUID advisorId, TicketPriority priority, Instant lowPriorityThreshold, Instant mediumPriorityThreshold) {
        if (priority == null) {
            return ticketRepository.findActiveForAdvisor(advisorId);
        }
        return switch (priority) {
            case LOW -> ticketRepository.findActiveForAdvisorStartedAfter(advisorId, lowPriorityThreshold);
            case MEDIUM -> ticketRepository.findActiveForAdvisorStartedBetween(advisorId, lowPriorityThreshold, mediumPriorityThreshold);
            case HIGH -> ticketRepository.findActiveForAdvisorStartedBeforeOrEqual(advisorId, mediumPriorityThreshold);
        };
    }

    private List<TicketEntity> listClosedTicketsForAdvisor(UUID advisorId, TicketPriority priority) {
        List<TicketEntity> closed = ticketRepository.findClosedForAdvisor(advisorId);
        if (priority == null) {
            return closed;
        }
        return closed.stream()
                .filter(t -> t.getPriority() == priority)
                .toList();
    }

    private List<TicketEntity> listTicketsForStudent(
            UUID studentId, Boolean active, TicketPriority priority) {
        List<TicketEntity> tickets = active == null
                ? ticketRepository.findAllByStudent(studentId)
                : ticketRepository.findAllByStudentAndActive(studentId, active);

        if (priority == null) {
            return tickets;
        }
        return tickets.stream()
                .filter(t -> t.getPriority() == priority)
                .toList();
    }
}
