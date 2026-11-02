package com.placement.placement_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProgressRequest(@NotBlank @Size(max = 200000) String progressJson) {
}
