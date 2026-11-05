package com.autoecole.student.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.autoecole.student.Student;
import com.autoecole.student.StudentStatus;

public record StudentResponse(
        Long id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String phone,
        String email,
        String address,
        String idNumber,
        String emergencyContact,
        StudentStatus status,
        Instant createdAt) {

    public static StudentResponse from(Student s) {
        return new StudentResponse(
                s.getId(), s.getFirstName(), s.getLastName(), s.getBirthDate(),
                s.getPhone(), s.getEmail(), s.getAddress(), s.getIdNumber(),
                s.getEmergencyContact(), s.getStatus(), s.getCreatedAt());
    }
}
