package com.autoecole.student.dto;

import java.time.LocalDate;

import com.autoecole.student.StudentStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentCreateRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        LocalDate birthDate,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email,
        String address,
        @Size(max = 50) String idNumber,
        @Size(max = 150) String emergencyContact,
        StudentStatus status) {

    public StudentCreateRequest {
        if (status == null) {
            status = StudentStatus.ACTIVE;
        }
    }
}
