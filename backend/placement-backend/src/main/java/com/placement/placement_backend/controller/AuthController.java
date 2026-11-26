package com.placement.placement_backend.controller;

import com.placement.placement_backend.dto.LoginRequest;
import com.placement.placement_backend.dto.LoginResponse;
import com.placement.placement_backend.dto.RegisterRequest;
import com.placement.placement_backend.service.AuthService;
import com.placement.placement_backend.service.FirebaseIdentityService;
import com.placement.placement_backend.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final StudentService studentService;
    private final FirebaseIdentityService firebaseIdentityService;

    public AuthController(AuthService authService, StudentService studentService,
                          FirebaseIdentityService firebaseIdentityService) {
        this.authService = authService;
        this.studentService = studentService;
        this.firebaseIdentityService = firebaseIdentityService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        studentService.assertEmailAvailable(email);

        FirebaseIdentityService.RegisteredAccount account =
                firebaseIdentityService.createAccount(email, request.password());
        boolean profileSaved = false;
        try {
            studentService.register(request);
            profileSaved = true;
            firebaseIdentityService.sendVerificationEmail(account.idToken());
            return Map.of("message", "Verification email sent. Verify your email, then sign in.");
        } catch (RuntimeException ex) {
            if (profileSaved) {
                try {
                    studentService.deleteByEmail(email);
                } catch (RuntimeException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
            }
            try {
                firebaseIdentityService.deleteAccount(account.uid());
            } catch (RuntimeException rollbackError) {
                ex.addSuppressed(rollbackError);
            }
            throw ex;
        }
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/resend-verification")
    public Map<String, String> resendVerification(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        return Map.of("message", firebaseIdentityService.resendVerificationEmail(email, request.password()));
    }
}
