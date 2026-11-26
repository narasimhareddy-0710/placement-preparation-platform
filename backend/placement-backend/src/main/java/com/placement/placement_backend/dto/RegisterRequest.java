package com.placement.placement_backend.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        // BCrypt only uses the first 72 bytes, so cap the length there.
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Pattern(regexp = "^(?:\\+91)?[6-9][0-9]{9}$", message = "Enter a valid 10-digit Indian mobile number") String phone,
        @Size(max = 150) String college,
        @Size(max = 100) String branch,
        @Min(1) @Max(4) Integer currentYear,
        @Min(2020) @Max(2100) Integer graduationYear,
        @Size(max = 100) String selectedDomain
) {
}
