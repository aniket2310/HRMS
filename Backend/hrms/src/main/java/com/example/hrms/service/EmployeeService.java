package com.example.hrms.service;

import com.example.hrms.dto.EmployeeRequestDto;
import com.example.hrms.dto.EmployeeResponseDto;
import com.example.hrms.entity.Department;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.Role;
import com.example.hrms.entity.User;
import com.example.hrms.repository.DepartmentRepository;
import com.example.hrms.repository.EmployeeRepository;
import com.example.hrms.repository.RoleRepository;
import com.example.hrms.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository repo;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final long MAX_PHOTO_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final long MAX_AADHAR_SIZE = 10 * 1024 * 1024; // 10 MB

    public EmployeeService(EmployeeRepository repo,
                           DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- CRUD ----------

    public EmployeeResponseDto create(EmployeeRequestDto req) {
        Department dept = req.getDepartmentId() != null ?
                departmentRepository.findById(req.getDepartmentId())
                        .orElseThrow(() -> new IllegalArgumentException("Department not found"))
                : null;

        Employee e = Employee.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .joiningDate(req.getJoiningDate())
                .contactNumber(req.getContactNumber())
                .email(req.getEmail())
                .address(req.getAddress())
                .dob(req.getDob())
                .department(dept)
                .status("Active")
                .build();

        // If username is provided, create linked User and set userId
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            if (req.getPassword() == null || req.getPassword().isBlank()) {
                throw new IllegalArgumentException("Password must be provided when username is supplied");
            }

            if (userRepository.existsByUsername(req.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }

            if (req.getEmail() != null && userRepository.existsByEmail(req.getEmail())) {
                throw new IllegalArgumentException("Email already exists in users");
            }

            Role employeeRole = roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new IllegalArgumentException("EMPLOYEE role not found"));

            User user = User.builder()
                    .username(req.getUsername())
                    .email(req.getEmail() != null ? req.getEmail() : req.getUsername() + "@example.com")
                    .password(passwordEncoder.encode(req.getPassword()))
                    .active(true)
                    .roles(Set.of(employeeRole))
                    .build();

            User savedUser = userRepository.save(user);
            e.setUserId(savedUser.getId());
        }

        Employee saved = repo.save(e);
        return toDto(saved);
    }

    public Optional<EmployeeResponseDto> getById(Long id) {
        return repo.findById(id).map(this::toDto);
    }

    public Page<EmployeeResponseDto> list(int page, int size, String sortBy) {
        Pageable p = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return repo.findAll(p).map(this::toDto);
    }

    public Optional<EmployeeResponseDto> update(Long id, EmployeeRequestDto req) {
        return repo.findById(id).map(emp -> {
            emp.setFirstName(req.getFirstName());
            emp.setLastName(req.getLastName());
            emp.setContactNumber(req.getContactNumber());
            emp.setEmail(req.getEmail());
            emp.setAddress(req.getAddress());
            emp.setDob(req.getDob());
            emp.setJoiningDate(req.getJoiningDate());

            if (req.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(req.getDepartmentId())
                        .orElseThrow(() -> new IllegalArgumentException("Department not found"));
                emp.setDepartment(dept);
            } else {
                emp.setDepartment(null);
            }

            // Handle linked user creation / update
            // 1) If employee already has userId -> update user
            // 2) If no userId and username provided -> create new user
            if (emp.getUserId() != null) {
                userRepository.findById(emp.getUserId()).ifPresent(user -> {
                    // Update username/email/password safely
                    if (req.getUsername() != null && !req.getUsername().isBlank()
                            && !req.getUsername().equals(user.getUsername())) {
                        if (userRepository.existsByUsername(req.getUsername())) {
                            throw new IllegalArgumentException("Username already exists");
                        }
                        user.setUsername(req.getUsername());
                    }
                    if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
                        if (userRepository.existsByEmail(req.getEmail())) {
                            throw new IllegalArgumentException("Email already exists in users");
                        }
                        user.setEmail(req.getEmail());
                    }
                    if (req.getPassword() != null && !req.getPassword().isBlank()) {
                        user.setPassword(passwordEncoder.encode(req.getPassword()));
                    }
                    userRepository.save(user);
                });
            } else {
                // create user if username provided in update
                if (req.getUsername() != null && !req.getUsername().isBlank()) {
                    if (req.getPassword() == null || req.getPassword().isBlank()) {
                        throw new IllegalArgumentException("Password must be provided when username is supplied");
                    }
                    if (userRepository.existsByUsername(req.getUsername())) {
                        throw new IllegalArgumentException("Username already exists");
                    }
                    if (req.getEmail() != null && userRepository.existsByEmail(req.getEmail())) {
                        throw new IllegalArgumentException("Email already exists in users");
                    }
                    Role employeeRole = roleRepository.findByName("EMPLOYEE")
                            .orElseThrow(() -> new IllegalArgumentException("EMPLOYEE role not found"));
                    User user = User.builder()
                            .username(req.getUsername())
                            .email(req.getEmail() != null ? req.getEmail() : req.getUsername() + "@example.com")
                            .password(passwordEncoder.encode(req.getPassword()))
                            .active(true)
                            .roles(Set.of(employeeRole))
                            .build();
                    User savedUser = userRepository.save(user);
                    emp.setUserId(savedUser.getId());
                }
            }

            if (req.getRemoveProfilePhoto() != null && req.getRemoveProfilePhoto()) {
                emp.setProfilePhoto(null);
                emp.setProfilePhotoFilename(null);
                emp.setProfilePhotoContentType(null);
            }

            if (req.getRemoveAadharPdf() != null && req.getRemoveAadharPdf()) {
                emp.setAadharPdf(null);
                emp.setAadharPdfFilename(null);
                emp.setAadharPdfContentType(null);
            }

            return toDto(repo.save(emp));
        });
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    // ---------- FILE METHODS ----------
    // (unchanged; keep your existing file methods)
    public void uploadProfilePhoto(Long empId, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Profile photo is empty");
        if (file.getSize() > MAX_PHOTO_SIZE)
            throw new IllegalArgumentException("Profile photo size exceeds 5MB");
        if (file.getContentType() == null || !file.getContentType().startsWith("image/"))
            throw new IllegalArgumentException("Profile photo must be an image");

        Employee emp = repo.findById(empId).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        emp.setProfilePhoto(file.getBytes());
        emp.setProfilePhotoFilename(file.getOriginalFilename());
        emp.setProfilePhotoContentType(file.getContentType());
        repo.save(emp);
    }

    public FileData downloadProfilePhoto(Long empId) {
        Employee emp = repo.findById(empId).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        if (emp.getProfilePhoto() == null)
            throw new IllegalArgumentException("Profile photo not found");
        return new FileData(emp.getProfilePhoto(), emp.getProfilePhotoFilename(), emp.getProfilePhotoContentType());
    }

    public void deleteProfilePhoto(Long empId) {
        Employee emp = repo.findById(empId).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        emp.setProfilePhoto(null);
        emp.setProfilePhotoFilename(null);
        emp.setProfilePhotoContentType(null);
        repo.save(emp);
    }

    public void uploadAadharPdf(Long empId, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Aadhar PDF is empty");
        if (file.getSize() > MAX_AADHAR_SIZE)
            throw new IllegalArgumentException("Aadhar PDF exceeds 10MB");
        if (!"application/pdf".equalsIgnoreCase(file.getContentType()))
            throw new IllegalArgumentException("Aadhar must be a PDF file");

        Employee emp = repo.findById(empId).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        emp.setAadharPdf(file.getBytes());
        emp.setAadharPdfFilename(file.getOriginalFilename());
        emp.setAadharPdfContentType(file.getContentType());
        repo.save(emp);
    }

    public FileData downloadAadharPdf(Long empId) {
        Employee emp = repo.findById(empId).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        if (emp.getAadharPdf() == null)
            throw new IllegalArgumentException("Aadhar PDF not found");
        return new FileData(emp.getAadharPdf(), emp.getAadharPdfFilename(), emp.getAadharPdfContentType());
    }

    public void deleteAadharPdf(Long empId) {
        Employee emp = repo.findById(empId).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        emp.setAadharPdf(null);
        emp.setAadharPdfFilename(null);
        emp.setAadharPdfContentType(null);
        repo.save(emp);
    }

    // ---------- HELPER ----------

    public record FileData(byte[] data, String filename, String contentType) {}

    private EmployeeResponseDto toDto(Employee e) {
        Long deptId = e.getDepartment() != null ? e.getDepartment().getDepartmentId() : null;
        String deptName = e.getDepartment() != null ? e.getDepartment().getName() : null;
        return EmployeeResponseDto.builder()
                .empId(e.getEmpId())
                .userId(e.getUserId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .departmentId(deptId)
                .departmentName(deptName)
                .joiningDate(e.getJoiningDate())
                .contactNumber(e.getContactNumber())
                .email(e.getEmail())
                .address(e.getAddress())
                .dob(e.getDob())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .profilePhotoFilename(e.getProfilePhotoFilename())
                .profilePhotoContentType(e.getProfilePhotoContentType())
                .aadharPdfFilename(e.getAadharPdfFilename())
                .aadharPdfContentType(e.getAadharPdfContentType())
                .build();
    }
}
