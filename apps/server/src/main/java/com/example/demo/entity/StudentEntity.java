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
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("STUDENT")
@Getter
@Setter
@NoArgsConstructor
public class StudentEntity extends AccountEntity {

    @Column(name = "student_code", nullable = false, unique = true, length = 30)
    private String studentCode;

    @Column(name = "career", nullable = false, length = 100)
    private String career;

    @Column(name = "current_semester", nullable = false)
    private Integer currentSemester;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Override
    public AccountType getAccountType() {
        return AccountType.STUDENT;
    }
}
