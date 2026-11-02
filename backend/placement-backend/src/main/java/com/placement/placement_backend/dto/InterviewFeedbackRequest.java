package com.placement.placement_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InterviewFeedbackRequest(
        @NotBlank @Size(max = 30) String role,
        @NotBlank @Size(max = 500) String question,
        @NotBlank @Size(max = 6000) String answer) {
}
