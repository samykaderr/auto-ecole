package com.autoecole.student;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.autoecole.student.dto.StudentCreateRequest;
import com.autoecole.student.dto.StudentResponse;
import com.autoecole.student.dto.StudentUpdateRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students")
public class StudentController {

    private final StudentService students;

    public StudentController(StudentService students) {
        this.students = students;
    }

    /** GET /api/students?query=&status=&page=&size=&sort= (pagination Spring, doc §8). */
    @GetMapping
    public Page<StudentResponse> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) StudentStatus status,
            Pageable pageable) {
        return students.search(query, status, pageable).map(StudentResponse::from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody StudentCreateRequest request,
                                  UriComponentsBuilder uriBuilder) {
        Student student = students.create(request);
        URI location = uriBuilder.path("/api/students/{id}").buildAndExpand(student.getId()).toUri();
        return StudentResponse.from(student); // location exposée dans l'en-tête
    }

    @GetMapping("/{id}")
    public StudentResponse get(@PathVariable Long id) {
        return StudentResponse.from(students.get(id));
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        return StudentResponse.from(students.update(id, request));
    }
}
