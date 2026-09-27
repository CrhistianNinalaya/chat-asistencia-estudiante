package com.example.demo.entity;

import com.example.demo.enums.AccountType;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "advisors")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("ADVISOR")
@Getter
@Setter
@NoArgsConstructor
public class AdvisorEntity extends AccountEntity {

    public static final int DEFAULT_MAX_CONCURRENT_TICKETS = 3;

    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @Column(name = "max_concurrent_tickets", nullable = false)
    private Integer maxConcurrentTickets = DEFAULT_MAX_CONCURRENT_TICKETS;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Override
    public AccountType getAccountType() {
        return AccountType.ADVISOR;
    }
}
