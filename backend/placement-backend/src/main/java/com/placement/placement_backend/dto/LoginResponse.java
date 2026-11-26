package com.placement.placement_backend.dto;

public record LoginResponse(
        String token,
        String tokenType,
        String studentId,
        String fullName,
        String email,
        String role
) {
}
