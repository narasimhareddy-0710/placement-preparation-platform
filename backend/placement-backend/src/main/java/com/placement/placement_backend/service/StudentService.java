package com.placement.placement_backend.service;

import com.placement.placement_backend.dto.RegisterRequest;
import com.placement.placement_backend.dto.UpdateProfileRequest;
import com.placement.placement_backend.entity.Role;
import com.placement.placement_backend.entity.Student;
import com.placement.placement_backend.exception.EmailAlreadyExistsException;
import com.placement.placement_backend.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Student register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (studentRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        Student student = new Student();
        student.setFullName(request.fullName().trim());
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode(request.password()));
        student.setRole(Role.STUDENT);   // the client can never choose its own role
        student.setPhone(request.phone());
        student.setCollege(request.college());
        student.setBranch(request.branch());
        student.setCurrentYear(request.currentYear());
        student.setGraduationYear(request.graduationYear());
        student.setSelectedDomain(request.selectedDomain());

        return studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public Student getByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated student no longer exists"));
    }

    @Transactional
    public Student updateProfile(String email, UpdateProfileRequest request) {
        Student student = getByEmail(email);
        student.setFullName(request.fullName().trim());
        student.setPhone(request.phone());
        student.setCollege(request.college());
        student.setBranch(request.branch());
        student.setCurrentYear(request.currentYear());
        student.setGraduationYear(request.graduationYear());
        student.setSelectedDomain(request.selectedDomain());
        return student;   // saved automatically when the transaction commits
    }

    @Transactional(readOnly = true)
    public String getProgress(String email) {
        return getByEmail(email).getProgressJson();
    }

    @Transactional
    public String saveProgress(String email, String progressJson) {
        Student student = getByEmail(email);
        student.setProgressJson(progressJson);
        return student.getProgressJson();
    }
}
