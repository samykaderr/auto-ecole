package com.autoecole.student.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.autoecole.student.Student;
import com.autoecole.student.StudentStatus;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // Recherche + filtrage : toujours bornées par le tenant courant (doc §5.2)
    @Query("""
            select s from Student s
            where s.schoolId = :schoolId
              and (:#{#query == null or #query.trim().isEmpty()} = true
                   or lower(s.firstName) like lower(concat('%', :query, '%'))
                   or lower(s.lastName) like lower(concat('%', :query, '%'))
                   or lower(s.email) like lower(concat('%', :query, '%')))
              and (:#{#status == null} = true or s.status = :status)
            """)
    Page<Student> search(@Param("schoolId") Long schoolId,
                         @Param("query") String query,
                         @Param("status") StudentStatus status,
                         Pageable pageable);

    Optional<Student> findByIdAndSchoolId(Long id, Long schoolId);

    boolean existsByIdAndSchoolId(Long id, Long schoolId);

    List<Student> findBySchoolId(Long schoolId);
}
