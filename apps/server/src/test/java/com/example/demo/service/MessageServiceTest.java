package com.example.demo.service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import com.example.demo.dto.MessageDto;
import com.example.demo.dto.TicketDto;
import com.example.demo.entity.AdvisorEntity;
import com.example.demo.entity.MessageEntity;
import com.example.demo.entity.StudentEntity;
import com.example.demo.entity.TicketActivityEntity;
import com.example.demo.entity.TicketAssignmentHistoryEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.enums.AccountType;
import com.example.demo.enums.TicketStatus;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.TicketActivityRepository;
import com.example.demo.repository.TicketAssignmentHistoryRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.impl.MessageServiceImpl;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TicketActivityRepository ticketActivityRepository;

    @Mock
    private TicketAssignmentHistoryRepository assignmentHistoryRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageServiceImpl messageService;

    private UserPrincipal createUser(UUID id, AccountType accountType) {
        return new UserPrincipal(
                id,
                "Test",
                "User",
                "user@test.com",
                "password",
                accountType,
                Collections.emptyList()
        );
    }

    @Test
    @DisplayName("Should assign advisor, record history and transition OPEN ticket to IN_PROGRESS upon initial reply")
    void sendMessage_WhenAdvisorRepliesToOpenTicket_AssignsAndBroadcastsStatus() {
        // Arrange
        UUID advisorId = UUID.randomUUID();
        UserPrincipal advisorUser = createUser(advisorId, AccountType.ADVISOR);
        AdvisorEntity advisor = new AdvisorEntity();
        advisor.setId(advisorId);

        UUID studentId = UUID.randomUUID();
        StudentEntity student = new StudentEntity();
        student.setId(studentId);

        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setGeneratedBy(student);
        ticket.setAssignedTo(null);

        MessageDto.SendRequest request = new MessageDto.SendRequest("Hello, I am here to help.");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(accountRepository.findById(advisorId)).thenReturn(Optional.of(advisor));
        when(accountRepository.getReferenceById(advisorId)).thenReturn(advisor);
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any(MessageEntity.class))).thenAnswer(invocation -> {
            MessageEntity msg = invocation.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });

        // Act
        MessageDto.Response response = messageService.sendMessage(request, ticketId, advisorUser);

        // Assert
        assertNotNull(response);
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        assertEquals(advisorId, ticket.getAssignedTo().getId());
        verify(assignmentHistoryRepository).save(any(TicketAssignmentHistoryEntity.class));
        verify(ticketActivityRepository, times(2)).save(any(TicketActivityEntity.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/tickets/" + ticketId + "/status"), any(TicketDto.Response.class));
    }

    @Test
    @DisplayName("Should transition ASSIGNED ticket to IN_PROGRESS and broadcast when assigned advisor replies")
    void sendMessage_WhenAdvisorRepliesToAssignedTicket_TransitionsToInProgress() {
        // Arrange
        UUID advisorId = UUID.randomUUID();
        UserPrincipal advisorUser = createUser(advisorId, AccountType.ADVISOR);
        AdvisorEntity advisor = new AdvisorEntity();
        advisor.setId(advisorId);

        UUID studentId = UUID.randomUUID();
        StudentEntity student = new StudentEntity();
        student.setId(studentId);

        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setGeneratedBy(student);
        ticket.setAssignedTo(advisor);

        MessageDto.SendRequest request = new MessageDto.SendRequest("Starting consultation now.");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(accountRepository.findById(advisorId)).thenReturn(Optional.of(advisor));
        when(accountRepository.getReferenceById(advisorId)).thenReturn(advisor);
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any(MessageEntity.class))).thenAnswer(invocation -> {
            MessageEntity msg = invocation.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });

        // Act
        MessageDto.Response response = messageService.sendMessage(request, ticketId, advisorUser);

        // Assert
        assertNotNull(response);
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        verify(ticketActivityRepository).save(any(TicketActivityEntity.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/tickets/" + ticketId + "/status"), any(TicketDto.Response.class));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when student reads another student ticket messages")
    void getMessages_WhenStudentIsNotTicketOwner_ThrowsAccessDeniedException() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UserPrincipal studentUser = createUser(studentId, AccountType.STUDENT);

        UUID otherStudentId = UUID.randomUUID();
        StudentEntity otherStudent = new StudentEntity();
        otherStudent.setId(otherStudentId);

        UUID ticketId = UUID.randomUUID();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setGeneratedBy(otherStudent);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> messageService.getMessagesByTicketId(ticketId, null, 10, studentUser)
        );
        assertEquals("Students can only access their own tickets", exception.getMessage());
    }
}
