package com.placement.placement_backend.dto;

import com.placement.placement_backend.entity.Student;

/** What the API returns about a student. It never contains the password or its hash. */
public record StudentResponse(
        String id,
        String fullName,
        String email,
        String role,
        String phone,
        String college,
        String branch,
        Integer currentYear,
        Integer graduationYear,
        String selectedDomain
) {
    public static StudentResponse from(Student s) {
        return new StudentResponse(
                s.getId(), s.getFullName(), s.getEmail(), s.getRole().name(),
                s.getPhone(), s.getCollege(), s.getBranch(),
                s.getCurrentYear(), s.getGraduationYear(), s.getSelectedDomain());
    }
}
