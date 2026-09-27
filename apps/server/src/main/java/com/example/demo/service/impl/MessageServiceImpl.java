package com.example.demo.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.MessageDto;
import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.AdvisorEntity;
import com.example.demo.entity.MessageEntity;
import com.example.demo.entity.StudentEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.MessageService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final TicketRepository ticketRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MessageDto.Response> getMessagesByChatId(UUID ticketId) {
        return messageRepository.findAllByChat_IdOrderBySentAtAsc(ticketId)
                .stream()
                .map(MessageDto.Response::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public MessageDto.Response sendMessage(MessageDto.SendRequest request, UUID ticketId, UserPrincipal user) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));

        if (!ticket.isActive() || ticket.getClosedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send messages to a closed ticket");
        }

        AccountEntity account = accountRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + user.getId()));

        switch (account) {
            case StudentEntity student ->
                validateStudent(ticket, student);
            case AdvisorEntity advisor ->
                assignAdvisorIfNeeded(ticket, advisor);
            case null, default ->
                throw new AccessDeniedException("User role is not authorized to send messages");
        }

        MessageEntity message = new MessageEntity();
        message.setContent(request.content().trim());
        message.setChat(ticket);
        message.setAccount(account);

        MessageEntity saved = messageRepository.save(message);

        return MessageDto.Response.fromEntity(saved);
    }

    private void validateStudent(TicketEntity ticket, StudentEntity student) {
        if (!ticket.getGeneratedBy().getId().equals(student.getId())) {
            throw new AccessDeniedException("Students can only send messages to their own tickets");
        }
    }

    private void assignAdvisorIfNeeded(TicketEntity ticket, AdvisorEntity advisor) {
        if (ticket.getAssignedTo() == null) {
            ticket.setAssignedTo(advisor);
            ticketRepository.save(ticket);
        } else if (!ticket.getAssignedTo().getId().equals(advisor.getId())) {
            throw new AccessDeniedException("Advisors cannot send messages to tickets assigned to another advisor");
        }
    }
}
