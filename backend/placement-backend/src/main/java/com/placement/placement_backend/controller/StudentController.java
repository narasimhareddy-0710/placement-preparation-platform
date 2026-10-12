
package com.placement.placement_backend.controller;

import com.placement.placement_backend.entity.Student;
import com.placement.placement_backend.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(
            @RequestBody Student student) {

        try {
            Student savedStudent =
                    studentService.registerStudent(student);

            // Never return the password or its hash to the client.
            savedStudent.setPassword(null);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedStudent);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }
}
