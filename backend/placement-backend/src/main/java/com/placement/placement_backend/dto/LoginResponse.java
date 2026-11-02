package com.placement.placement_backend.dto;

public record LoginResponse(
        String token,
        String tokenType,
        Long studentId,
        String fullName,
        String email,
        String role
) {
}
