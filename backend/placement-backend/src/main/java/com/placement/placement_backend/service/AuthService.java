package com.placement.placement_backend.service;

import com.placement.placement_backend.dto.LoginRequest;
import com.placement.placement_backend.dto.LoginResponse;
import com.placement.placement_backend.entity.Student;
import com.placement.placement_backend.exception.EmailAlreadyExistsException;
import com.placement.placement_backend.exception.EmailNotVerifiedException;
import com.placement.placement_backend.repository.StudentRepository;
import com.placement.placement_backend.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final JwtService jwtService;
    private final FirebaseIdentityService firebaseIdentityService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(StudentRepository studentRepository,
                       JwtService jwtService,
                       FirebaseIdentityService firebaseIdentityService,
                       PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.jwtService = jwtService;
        this.firebaseIdentityService = firebaseIdentityService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        FirebaseIdentityService.SignedInAccount identity;
        try {
            identity = firebaseIdentityService.signIn(email, request.password());
        } catch (BadCredentialsException authenticationFailure) {
            migrateLegacyFirestoreAccount(email, request.password(), authenticationFailure);
            throw authenticationFailure;
        }
        if (!identity.emailVerified()) throw new EmailNotVerifiedException();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Replacing the document removes a legacy password hash once Firebase Auth owns the credential.
        if (studentRepository.findLegacyPasswordHash(email).isPresent()) studentRepository.save(student);

        return new LoginResponse(
                jwtService.generateToken(student.getEmail()),
                "Bearer",
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                student.getRole().name());
    }

    private void migrateLegacyFirestoreAccount(String email, String password,
                                               BadCredentialsException authenticationFailure) {
        Student legacyStudent = studentRepository.findByEmail(email).orElse(null);
        String legacyHash = studentRepository.findLegacyPasswordHash(email).orElse(null);
        if (legacyStudent == null || legacyHash == null || !passwordEncoder.matches(password, legacyHash)) return;

        try {
            FirebaseIdentityService.RegisteredAccount account = firebaseIdentityService.createAccount(email, password);
            studentRepository.save(legacyStudent);
            firebaseIdentityService.sendVerificationEmail(account.idToken());
            throw new EmailNotVerifiedException();
        } catch (EmailAlreadyExistsException alreadyMigrated) {
            // The Firebase account exists already; the supplied password is not its current credential.
        }
    }
}
