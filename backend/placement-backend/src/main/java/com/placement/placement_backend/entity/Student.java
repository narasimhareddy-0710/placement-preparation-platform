package com.placement.placement_backend.entity;

import java.time.Instant;

/** Student document stored in the Firestore "students" collection. */
public class Student {

    private String id;
    private String fullName;
    private String email;
    /** BCrypt hash, never the plain password. */
    private String password;
    private Role role = Role.STUDENT;
    private String phone;
    private String college;
    private String branch;
    private Integer currentYear;
    private Integer graduationYear;
    private String selectedDomain;
    /** Per-student learning state synced across the student's devices. */
    private String progressJson = "{}";
    /** ISO-8601 UTC timestamp (a Firestore-friendly scalar value). */
    private String createdAt;

    public Student() { }

    public void initializeCreatedAt() {
        if (createdAt == null || createdAt.isBlank()) createdAt = Instant.now().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public Integer getCurrentYear() { return currentYear; }
    public void setCurrentYear(Integer currentYear) { this.currentYear = currentYear; }
    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }
    public String getSelectedDomain() { return selectedDomain; }
    public void setSelectedDomain(String selectedDomain) { this.selectedDomain = selectedDomain; }
    public String getProgressJson() { return progressJson == null ? "{}" : progressJson; }
    public void setProgressJson(String progressJson) { this.progressJson = progressJson; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
