package com.example.demo.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.TicketDto;
import com.example.demo.entity.StudentEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.repository.TicketRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.TicketService;
import com.example.enums.AccountType;
import com.example.enums.TicketPriority;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    public List<TicketDto.Response> listTickets(Boolean active, TicketPriority priority, UserPrincipal user) {
        if (user == null) {
            return Collections.emptyList();
        }

        Instant now = Instant.now();
        Instant twoDaysAgo = now.minus(2, ChronoUnit.DAYS);
        Instant fourDaysAgo = now.minus(4, ChronoUnit.DAYS);

        List<TicketEntity> entities;
        if (user.getAccountType() == AccountType.ADVISOR) {
            entities = listTicketsForAdvisor(user.getId(), active, priority, twoDaysAgo, fourDaysAgo);
        } else {
            entities = listTicketsForStudent(user.getId(), active, priority);
        }

        return entities.stream()
                .map(TicketDto.Response::fromEntity)
                .toList();
    }

    @Override
    public TicketDto.Response createTicket(TicketDto.CreateRequest request, UserPrincipal user) {
        if (user == null || user.getAccountType() != AccountType.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can create tickets");
        }

        StudentEntity student = new StudentEntity();
        student.setId(user.getId());
        student.setFirstName(user.getFirstName());
        student.setLastName(user.getLastName());
        student.setEmail(user.getEmail());

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
            UUID advisorId, Boolean active, TicketPriority priority, Instant twoDaysAgo, Instant fourDaysAgo) {
        boolean isActive = active == null || Boolean.TRUE.equals(active);
        if (isActive) {
            return listActiveTicketsForAdvisor(advisorId, priority, twoDaysAgo, fourDaysAgo);
        }
        return listClosedTicketsForAdvisor(advisorId, priority);
    }

    private List<TicketEntity> listActiveTicketsForAdvisor(
            UUID advisorId, TicketPriority priority, Instant twoDaysAgo, Instant fourDaysAgo) {
        if (priority == null) {
            return ticketRepository.findActiveForAdvisor(advisorId);
        }
        return switch (priority) {
            case LOW -> ticketRepository.findActiveForAdvisorStartedAfter(advisorId, twoDaysAgo);
            case MEDIUM -> ticketRepository.findActiveForAdvisorStartedBetween(advisorId, twoDaysAgo, fourDaysAgo);
            case HIGH -> ticketRepository.findActiveForAdvisorStartedBeforeOrEqual(advisorId, fourDaysAgo);
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

    @Override
    public List<TicketEntity> findAll() {
        return ticketRepository.findAll();
    }

    @Override
    public List<TicketEntity> findByAccount(UUID accountId) {
        if (accountId == null) {
            return Collections.emptyList();
        }
        return ticketRepository.findAllByGeneratedBy_Id(accountId);
    }

    @Override
    public TicketEntity save(TicketEntity ticket) {
        return ticketRepository.save(ticket);
    }
}
