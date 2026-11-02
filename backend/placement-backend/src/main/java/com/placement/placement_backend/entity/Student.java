package com.placement.placement_backend.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt hash, never the plain password. */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.STUDENT;

    private String phone;
    private String college;
    private String branch;

    /** Current year of study (1-4). The platform targets students from 2nd year. */
    private Integer currentYear;

    private Integer graduationYear;

    /** e.g. "Java Full Stack Developer". Becomes a Domain entity later. */
    private String selectedDomain;

    /** Per-student learning state synced across the student's devices. */
    @Lob
    private String progressJson = "{}";

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Student() {
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Instant getCreatedAt() { return createdAt; }
}
