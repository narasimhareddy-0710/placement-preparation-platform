package com.placement.placement_backend.service;

import com.placement.placement_backend.dto.LoginRequest;
import com.placement.placement_backend.dto.LoginResponse;
import com.placement.placement_backend.entity.Student;
import com.placement.placement_backend.repository.StudentRepository;
import com.placement.placement_backend.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(StudentRepository studentRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), student.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return new LoginResponse(
                jwtService.generateToken(student.getEmail()),
                "Bearer",
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                student.getRole().name());
    }
}
