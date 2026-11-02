package com.placement.placement_backend.controller;

import com.placement.placement_backend.dto.StudentResponse;
import com.placement.placement_backend.dto.UpdateProfileRequest;
import com.placement.placement_backend.dto.ProgressRequest;
import com.placement.placement_backend.dto.ProgressResponse;
import com.placement.placement_backend.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** The logged-in student's own profile (identified by the JWT, not by an id in the URL). */
    @GetMapping("/me")
    public StudentResponse me(Authentication authentication) {
        return StudentResponse.from(studentService.getByEmail(authentication.getName()));
    }

    @PutMapping("/me")
    public StudentResponse updateMe(Authentication authentication,
                                    @Valid @RequestBody UpdateProfileRequest request) {
        return StudentResponse.from(
                studentService.updateProfile(authentication.getName(), request));
    }

    @GetMapping("/me/progress")
    public ProgressResponse progress(Authentication authentication) {
        return new ProgressResponse(studentService.getProgress(authentication.getName()));
    }

    @PutMapping("/me/progress")
    public ProgressResponse saveProgress(Authentication authentication,
                                         @Valid @RequestBody ProgressRequest request) {
        return new ProgressResponse(studentService.saveProgress(authentication.getName(), request.progressJson()));
    }
}
