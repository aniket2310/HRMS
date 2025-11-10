package com.example.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long empId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "contact_number")
    private String contactNumber;

    private String email;
    private String address;
    private LocalDate dob;
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---------- FILE FIELDS ----------

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "profile_photo", columnDefinition = "LONGBLOB")
    private byte[] profilePhoto;

    @Column(name = "profile_photo_filename")
    private String profilePhotoFilename;

    @Column(name = "profile_photo_content_type")
    private String profilePhotoContentType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "aadhar_pdf", columnDefinition = "LONGBLOB")
    private byte[] aadharPdf;

    @Column(name = "aadhar_pdf_filename")
    private String aadharPdfFilename;

    @Column(name = "aadhar_pdf_content_type")
    private String aadharPdfContentType;

    // ---------- TIMESTAMP HOOKS ----------
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
