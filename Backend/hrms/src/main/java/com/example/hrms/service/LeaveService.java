package com.example.hrms.service;

import com.example.hrms.dto.LeaveRequestDto;
import com.example.hrms.dto.LeaveResponseDto;
import com.example.hrms.entity.*;
import com.example.hrms.repository.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepo;
    private final EmployeeRepository employeeRepo;
    private final LeaveTypeRepository leaveTypeRepo;
    private final UserRepository userRepo;

    public LeaveService(LeaveRepository leaveRepo,
                        EmployeeRepository employeeRepo,
                        LeaveTypeRepository leaveTypeRepo,
                        UserRepository userRepo) {
        this.leaveRepo = leaveRepo;
        this.employeeRepo = employeeRepo;
        this.leaveTypeRepo = leaveTypeRepo;
        this.userRepo = userRepo;
    }

    /**
     * Create a leave.
     * If employeeId in dto is null, the method will try to resolve current user's employee record.
     * EMPLOYEEs can only create for themselves; ADMIN/HR may create for any employee.
     */
    @Transactional
    public LeaveResponseDto create(LeaveRequestDto dto, Authentication auth) {
        // validate leaveType
        LeaveType lt = leaveTypeRepo.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));

        // resolve employee
        Employee employee = resolveEmployeeForCreate(dto.getEmployeeId(), auth);

        // validate date range
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("endDate must be on or after startDate");
        }

        // optional: check overlapping leaves for same employee (simple check)
        var overlaps = leaveRepo.findByEmployeeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                employee, dto.getEndDate(), dto.getStartDate());
        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException("There is an overlapping leave for this employee");
        }

        Leave leave = Leave.builder()
                .employee(employee)
                .leaveType(lt)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .status("Pending")
                .build();

        Leave saved = leaveRepo.save(leave);
        return toDto(saved);
    }

    public Optional<LeaveResponseDto> getById(Long id) {
        return leaveRepo.findById(id).map(this::toDto);
    }

    /**
     * List leaves with optional filters.
     * - If employeeId provided, returns leaves for that employee.
     * - If status provided, filters by status.
     * - dateFrom/dateTo filter leaves overlapping that range.
     */
    public Page<LeaveResponseDto> list(Integer page, Integer size, String sortBy,
                                       Long employeeId, String status,
                                       LocalDate dateFrom, LocalDate dateTo) {
        Pageable p = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size, Sort.by(sortBy == null ? "leaveId" : sortBy).ascending());

        // filter combinations: simplest approach - fetch all and filter via repo queries for common cases
        if (employeeId != null) {
            Employee emp = employeeRepo.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
            return leaveRepo.findByEmployee(emp, p).map(this::toDto);
        }
        if (status != null && !status.isBlank()) {
            return leaveRepo.findByStatus(status, p).map(this::toDto);
        }
        // fallback: return all (with paging)
        return leaveRepo.findAll(p).map(this::toDto);
    }

    /**
     * Update a leave (only allowed in Pending status).
     * Employees can update only their own pending leaves.
     * Admin/HR can update any.
     */
    @Transactional
    public Optional<LeaveResponseDto> update(Long id, LeaveRequestDto dto, Authentication auth) {
        return leaveRepo.findById(id).map(leave -> {
            // ownership check if caller is EMPLOYEE
            boolean callerIsEmployee = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
            if (callerIsEmployee && !isCurrentUserEmployeeMatch(leave.getEmployee(), auth)) {
                throw new IllegalArgumentException("Employees can only update their own leaves");
            }

            if (!"Pending".equalsIgnoreCase(leave.getStatus())) {
                throw new IllegalArgumentException("Only pending leaves can be updated");
            }

            // update fields: allow leaving employee unchanged unless admin provided different employeeId
            if (dto.getEmployeeId() != null) {
                Employee newEmp = employeeRepo.findById(dto.getEmployeeId())
                        .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
                leave.setEmployee(newEmp);
            }

            if (dto.getLeaveTypeId() != null) {
                LeaveType lt = leaveTypeRepo.findById(dto.getLeaveTypeId())
                        .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));
                leave.setLeaveType(lt);
            }

            if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
                throw new IllegalArgumentException("endDate must be on or after startDate");
            }
            leave.setStartDate(dto.getStartDate());
            leave.setEndDate(dto.getEndDate());
            leave.setReason(dto.getReason());

            Leave updated = leaveRepo.save(leave);
            return toDto(updated);
        });
    }

    /**
     * Delete a leave.
     * Employees can cancel their own pending leaves. Admin/HR can delete any.
     */
    public void delete(Long id, Authentication auth) {
        Leave leave = leaveRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Leave not found"));

        boolean callerIsEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        if (callerIsEmployee && !isCurrentUserEmployeeMatch(leave.getEmployee(), auth)) {
            throw new IllegalArgumentException("Employees can only delete their own leaves");
        }

        if (callerIsEmployee && !"Pending".equalsIgnoreCase(leave.getStatus())) {
            throw new IllegalArgumentException("Employees can only cancel pending leaves");
        }

        leaveRepo.deleteById(id);
    }

    /**
     * Change status (approve/reject). Only ADMIN or HR allowed.
     */
    @Transactional
    public LeaveResponseDto changeStatus(Long id, String newStatus, Authentication auth) {
        Leave leave = leaveRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Leave not found"));
        if (!("Approved".equalsIgnoreCase(newStatus) || "Rejected".equalsIgnoreCase(newStatus) || "Pending".equalsIgnoreCase(newStatus))) {
            throw new IllegalArgumentException("Invalid status");
        }

        // set approvedBy if approved
        if ("Approved".equalsIgnoreCase(newStatus) || "Rejected".equalsIgnoreCase(newStatus)) {
            var userOpt = userRepo.findByUsername(auth.getName());
            if (userOpt.isEmpty()) throw new IllegalArgumentException("Approver user not found");
            leave.setApprovedBy(userOpt.get());
        } else {
            leave.setApprovedBy(null);
        }

        leave.setStatus(newStatus);
        Leave updated = leaveRepo.save(leave);
        return toDto(updated);
    }

    // --- helpers ---
    private Employee resolveEmployeeForCreate(Long employeeId, Authentication auth) {
        if (employeeId != null) {
            return employeeRepo.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        }
        // no employeeId supplied: if caller is EMPLOYEE, resolve by user -> employee
        boolean callerIsEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        if (!callerIsEmployee) {
            throw new IllegalArgumentException("employeeId is required when creating on behalf");
        }
        // resolve user -> find employee by userId
        var userOpt = userRepo.findByUsername(auth.getName());
        if (userOpt.isEmpty()) throw new IllegalArgumentException("Current user not found");
        Long userId = userOpt.get().getId();
        return employeeRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Employee record not found for current user"));
    }

    private boolean isCurrentUserEmployeeMatch(Employee employee, Authentication auth) {
        var userOpt = userRepo.findByUsername(auth.getName());
        if (userOpt.isEmpty()) return false;
        Long userId = userOpt.get().getId();
        return employee.getUserId() != null && employee.getUserId().equals(userId);
    }

    private LeaveResponseDto toDto(Leave l) {
        return LeaveResponseDto.builder()
                .leaveId(l.getLeaveId())
                .employeeId(l.getEmployee() != null ? l.getEmployee().getEmpId() : null)
                .employeeName(l.getEmployee() != null ? (l.getEmployee().getFirstName() + " " + (l.getEmployee().getLastName() == null ? "" : l.getEmployee().getLastName())).trim() : null)
                .leaveTypeId(l.getLeaveType() != null ? l.getLeaveType().getLeaveTypeId() : null)
                .leaveTypeName(l.getLeaveType() != null ? l.getLeaveType().getName() : null)
                .startDate(l.getStartDate())
                .endDate(l.getEndDate())
                .reason(l.getReason())
                .status(l.getStatus())
                .approvedById(l.getApprovedBy() != null ? l.getApprovedBy().getId() : null)
                .approvedByUsername(l.getApprovedBy() != null ? l.getApprovedBy().getUsername() : null)
                .appliedOn(l.getAppliedOn())
                .build();
    }
}
