package com.example.demo.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.MessageDto;
import com.example.demo.dto.TicketDto;
import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.AdvisorEntity;
import com.example.demo.entity.MessageEntity;
import com.example.demo.entity.StudentEntity;
import com.example.demo.entity.TicketActivityEntity;
import com.example.demo.entity.TicketAssignmentHistoryEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.enums.TicketActivityType;
import com.example.demo.enums.TicketStatus;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.TicketActivityRepository;
import com.example.demo.repository.TicketAssignmentHistoryRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.MessageService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;

    private final MessageRepository messageRepository;
    private final TicketRepository ticketRepository;
    private final AccountRepository accountRepository;
    private final TicketActivityRepository ticketActivityRepository;
    private final TicketAssignmentHistoryRepository assignmentHistoryRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(readOnly = true)
    public MessageDto.PagedResponse getMessagesByTicketId(UUID ticketId, Instant before, int limit, UserPrincipal user) {
        TicketEntity ticket = getTicketOrThrow(ticketId);

        validateTicketReadAccess(ticket, user);

        int pageSize = Math.clamp(limit, MIN_PAGE_SIZE, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        List<MessageEntity> entities = before == null
                ? messageRepository.findRecentMessages(ticketId, pageable)
                : messageRepository.findMessagesBefore(ticketId, before, pageable);

        boolean hasMore = entities.size() > pageSize;
        List<MessageEntity> pagedEntities = hasMore
                ? entities.subList(0, pageSize)
                : entities;

        List<MessageDto.Response> messages = new ArrayList<>(pagedEntities.stream()
                .map(MessageDto.Response::fromEntity)
                .toList());
        Collections.reverse(messages);

        Instant nextCursor = hasMore && !pagedEntities.isEmpty()
                ? pagedEntities.getLast().getSentAt()
                : null;

        return new MessageDto.PagedResponse(messages, hasMore, nextCursor);
    }

    @Override
    @Transactional
    public MessageDto.Response sendMessage(MessageDto.SendRequest request, UUID ticketId, UserPrincipal user) {
        TicketEntity ticket = getTicketOrThrow(ticketId);
        AccountEntity account = getAccountOrThrow(user.getId());

        validateTicketIsActive(ticket);
        validateAndAssignSender(ticket, account);

        MessageEntity message = new MessageEntity();
        message.setContent(request.content().trim());
        message.setTicket(ticket);
        message.setAccount(account);

        MessageEntity saved = messageRepository.save(message);

        return MessageDto.Response.fromEntity(saved);
    }

    private TicketEntity getTicketOrThrow(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));
    }

    private AccountEntity getAccountOrThrow(UUID userId) {
        return accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + userId));
    }

    private void validateTicketIsActive(TicketEntity ticket) {
        if (!ticket.isActive() || ticket.getClosedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send messages to a closed ticket");
        }
    }

    private void validateTicketReadAccess(TicketEntity ticket, UserPrincipal user) {
        switch (user.getAccountType()) {
            case ADMIN -> {
                // Admins have access to all tickets, no additional validation needed
            }
            case STUDENT ->
                validateStudentOwnership(ticket, user.getId());
            case ADVISOR ->
                validateAdvisorReadAccess(ticket, user.getId());
            case null, default ->
                throw new AccessDeniedException("User role is not authorized to view messages");
        }
    }

    private void validateAndAssignSender(TicketEntity ticket, AccountEntity account) {
        switch (account) {
            case StudentEntity student ->
                validateStudentOwnership(ticket, student.getId());
            case AdvisorEntity advisor ->
                assignAdvisorIfNeeded(ticket, advisor);
            case null, default ->
                throw new AccessDeniedException("User role is not authorized to send messages");
        }
    }

    private void validateStudentOwnership(TicketEntity ticket, UUID studentId) {
        if (!ticket.getGeneratedBy().getId().equals(studentId)) {
            throw new AccessDeniedException("Students can only access their own tickets");
        }
    }

    private void validateAdvisorReadAccess(TicketEntity ticket, UUID advisorId) {
        if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().getId().equals(advisorId)) {
            throw new AccessDeniedException("Advisors can only view tickets assigned to them or unassigned tickets");
        }
    }

    private void assignAdvisorIfNeeded(TicketEntity ticket, AdvisorEntity advisor) {
        if (ticket.getAssignedTo() == null) {
            ticket.setAssignedTo(advisor);
            recordAssignment(ticket, advisor);
            if (ticket.getStatus() == TicketStatus.OPEN) {
                transitionStatus(ticket, TicketStatus.IN_PROGRESS, advisor.getId(), "Status transitioned from OPEN to IN_PROGRESS upon initial advisor reply");
            } else {
                ticketRepository.save(ticket);
            }
        } else if (!ticket.getAssignedTo().getId().equals(advisor.getId())) {
            throw new AccessDeniedException("Advisors cannot send messages to tickets assigned to another advisor");
        } else if (ticket.getStatus() == TicketStatus.ASSIGNED) {
            transitionStatus(ticket, TicketStatus.IN_PROGRESS, advisor.getId(), "Status transitioned from ASSIGNED to IN_PROGRESS upon advisor reply");
        }
    }

    private void transitionStatus(TicketEntity ticket, TicketStatus newStatus, UUID actorId, String reason) {
        ticket.setStatus(newStatus);
        ticket.setClosedAt(null);
        TicketEntity saved = ticketRepository.save(ticket);
        recordActivity(saved, actorId, TicketActivityType.STATUS_CHANGED, reason);
        messagingTemplate.convertAndSend("/topic/tickets/" + saved.getId() + "/status", TicketDto.Response.fromEntity(saved));
    }

    private void recordActivity(TicketEntity ticket, UUID actorId, TicketActivityType type, String details) {
        AccountEntity actor = actorId != null ? accountRepository.getReferenceById(actorId) : null;
        TicketActivityEntity activity = new TicketActivityEntity();
        activity.setTicket(ticket);
        activity.setActor(actor);
        activity.setActivityType(type);
        activity.setDetails(details);
        ticketActivityRepository.save(activity);
    }

    private void recordAssignment(TicketEntity ticket, AdvisorEntity advisor) {
        TicketAssignmentHistoryEntity history = new TicketAssignmentHistoryEntity();
        history.setTicket(ticket);
        history.setAdvisor(advisor);
        history.setAssignedAt(Instant.now());
        assignmentHistoryRepository.save(history);
        recordActivity(ticket, advisor.getId(), TicketActivityType.ADVISOR_ASSIGNED, "Advisor assigned to ticket upon replying");
    }
}
