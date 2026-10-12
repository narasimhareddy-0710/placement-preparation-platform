
package com.placement.placement_backend.service;

import com.placement.placement_backend.entity.Student;
import com.placement.placement_backend.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Student registerStudent(Student student) {
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException(
                    "Email is already registered");
        }

        student.setPassword(
                passwordEncoder.encode(student.getPassword()));

        return studentRepository.save(student);
    }
}
