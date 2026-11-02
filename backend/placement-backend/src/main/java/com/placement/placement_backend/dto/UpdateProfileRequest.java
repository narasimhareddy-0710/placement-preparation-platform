package com.placement.placement_backend.dto;

import jakarta.validation.constraints.*;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 20) String phone,
        @Size(max = 150) String college,
        @Size(max = 100) String branch,
        @Min(1) @Max(4) Integer currentYear,
        @Min(2020) @Max(2100) Integer graduationYear,
        @Size(max = 100) String selectedDomain
) {
}
