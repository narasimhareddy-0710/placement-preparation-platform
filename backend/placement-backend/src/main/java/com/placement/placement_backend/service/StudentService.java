package com.placement.placement_backend.service;

import com.placement.placement_backend.dto.RegisterRequest;
import com.placement.placement_backend.dto.UpdateProfileRequest;
import com.placement.placement_backend.entity.Role;
import com.placement.placement_backend.entity.Student;
import com.placement.placement_backend.exception.EmailAlreadyExistsException;
import com.placement.placement_backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        assertEmailAvailable(email);

        Student student = new Student();
        student.setId(UUID.randomUUID().toString());
        student.setFullName(request.fullName().trim());
        student.setEmail(email);
        student.setRole(Role.STUDENT);   // the client can never choose its own role
        student.setPhone(normalizePhone(request.phone()));
        student.setCollege(request.college());
        student.setBranch(request.branch());
        student.setCurrentYear(request.currentYear());
        student.setGraduationYear(request.graduationYear());
        student.setSelectedDomain(request.selectedDomain());
        student.initializeCreatedAt();

        return studentRepository.create(student);
    }

    public void assertEmailAvailable(String email) {
        if (studentRepository.existsByEmail(email.trim().toLowerCase(Locale.ROOT))) {
            throw new EmailAlreadyExistsException();
        }
    }

    public void deleteByEmail(String email) {
        studentRepository.deleteByEmail(email);
    }

    private String normalizePhone(String phone) {
        String normalized = phone.trim().replaceAll("[\\s()-]", "");
        return normalized.startsWith("+91") ? normalized : "+91" + normalized;
    }

    public Student getByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated student no longer exists"));
    }

    public Student updateProfile(String email, UpdateProfileRequest request) {
        Student student = getByEmail(email);
        student.setFullName(request.fullName().trim());
        student.setPhone(normalizePhone(request.phone()));
        student.setCollege(request.college());
        student.setBranch(request.branch());
        student.setCurrentYear(request.currentYear());
        student.setGraduationYear(request.graduationYear());
        student.setSelectedDomain(request.selectedDomain());
        return studentRepository.save(student);
    }

    public String getProgress(String email) {
        return getByEmail(email).getProgressJson();
    }

    public String saveProgress(String email, String progressJson) {
        Student student = getByEmail(email);
        student.setProgressJson(progressJson);
        return studentRepository.save(student).getProgressJson();
    }
}
