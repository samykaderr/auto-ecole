package com.autoecole.student;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoecole.student.dto.StudentCreateRequest;import com.autoecole.student.dto.StudentUpdateRequest;
import com.autoecole.student.repository.StudentRepository;
import com.autoecole.common.exception.NotFoundException;
import com.autoecole.common.tenant.TenantContext;
import com.autoecole.user.AppUserRepository;

/**
 * CRUD élèves, toujours borné au tenant courant (doc §5.2 : isolation multi-tenant).
 */
@Service
public class StudentService {

    private final StudentRepository students;
    private final AppUserRepository users;

    public StudentService(StudentRepository students, AppUserRepository users) {
        this.students = students;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public Page<Student> search(String query, StudentStatus status, Pageable pageable) {
        return students.search(TenantContext.require(), query, status, pageable);
    }


    @Transactional
    public Student create(StudentCreateRequest request) {
        Student student = new Student();
        apply(student, request.firstName(), request.lastName(), request.birthDate(), request.phone(),
                request.email(), request.address(), request.idNumber(), request.emergencyContact(),
                request.status());
        student.setSchoolId(TenantContext.require());
        return students.save(student);
    }

    @Transactional(readOnly = true)
    public Student get(Long id) {
        return students.findByIdAndSchoolId(id, TenantContext.require())
                .orElseThrow(() -> new NotFoundException("Élève", id));
    }

    @Transactional
    public Student update(Long id, StudentUpdateRequest request) {
        Student student = get(id);
        apply(student, request.firstName(), request.lastName(), request.birthDate(), request.phone(),
                request.email(), request.address(), request.idNumber(), request.emergencyContact(),
                request.status());
        return students.save(student);
    }

    private static void apply(Student student, String firstName, String lastName, java.time.LocalDate birthDate,
                              String phone, String email, String address, String idNumber,
                              String emergencyContact, StudentStatus status) {
        student.setFirstName(firstName.strip());
        student.setLastName(lastName.strip());
        student.setBirthDate(birthDate);
        student.setPhone(phone);
        student.setEmail(email);
        student.setAddress(address);
        student.setIdNumber(idNumber);
        student.setEmergencyContact(emergencyContact);
        student.setStatus(status);
    }
}
