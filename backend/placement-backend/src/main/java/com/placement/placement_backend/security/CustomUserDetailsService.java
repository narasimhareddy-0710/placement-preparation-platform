package com.placement.placement_backend.security;

import com.placement.placement_backend.entity.Student;
import com.placement.placement_backend.repository.StudentRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/** Lets Spring Security load a student by email. Also stops Spring Boot creating its default in-memory user. */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;

    public CustomUserDetailsService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Student student = studentRepository
                .findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(student.getEmail())
                .password(student.getPassword())
                .roles(student.getRole().name())   // becomes ROLE_STUDENT / ROLE_ADMIN
                .build();
    }
}
