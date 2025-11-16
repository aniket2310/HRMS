package com.example.hrms.service.impl;

import com.example.hrms.dto.AttendanceRequestDTO;
import com.example.hrms.dto.AttendanceResponseDTO;
import com.example.hrms.entity.Attendance;
import com.example.hrms.entity.Employee;
import com.example.hrms.repository.AttendanceRepository;
import com.example.hrms.repository.EmployeeRepository;
import com.example.hrms.service.AttendanceService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final EmployeeRepository employeeRepo;

    // ------------------------------------------------------
    // CHECK IN
    // ------------------------------------------------------
    @Override
    @Transactional
    public AttendanceResponseDTO checkIn(Long empId) {

        Employee employee = employeeRepo.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDate today = LocalDate.now();

        attendanceRepo.findByEmployeeAndDate(employee, today)
                .ifPresent(a -> {
                    throw new RuntimeException("Already checked in today!");
                });

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(today)
                .checkIn(LocalTime.now())
                .status("Present")
                .remarks("Checked in successfully")
                .build();

        Attendance saved = attendanceRepo.save(attendance);

        return toDTO(saved);
    }

    // ------------------------------------------------------
    // CHECK OUT
    // ------------------------------------------------------
    @Override
    @Transactional
    public AttendanceResponseDTO checkOut(Long empId) {

        Employee employee = employeeRepo.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Attendance attendance = attendanceRepo.findByEmployeeAndDate(employee, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("You haven't checked in yet!"));

        if (attendance.getCheckOut() != null)
            throw new RuntimeException("Already checked out today!");

        attendance.setCheckOut(LocalTime.now());
        attendance.setRemarks("Checked out successfully");

        Attendance saved = attendanceRepo.save(attendance);

        return toDTO(saved);
    }

    // ------------------------------------------------------
    // GET ATTENDANCE FOR SPECIFIC EMPLOYEE
    // ------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> getAttendanceByEmployee(Long empId) {

        return attendanceRepo.findAllByEmployee_EmpId(empId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------
    // GET ALL
    // ------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> getAllAttendance() {

        return attendanceRepo.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------
    // GET BY DATE
    // ------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> getAttendanceByDate(String date) {

        LocalDate target = LocalDate.parse(date);

        return attendanceRepo.findAllByDate(target)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------
    // UPDATE STATUS (HR / ADMIN)
    // ------------------------------------------------------
    @Override
    @Transactional
    public AttendanceResponseDTO updateStatus(Long attendanceId, AttendanceRequestDTO dto) {

        Attendance attendance = attendanceRepo.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            attendance.setStatus(dto.getStatus());
        }

        if (dto.getRemarks() != null && !dto.getRemarks().isBlank()) {
            attendance.setRemarks(dto.getRemarks());
        }

        Attendance saved = attendanceRepo.save(attendance);

        return toDTO(saved);
    }

    // ------------------------------------------------------
    // MAPPER - ENTITY → RESPONSE DTO
    // ------------------------------------------------------
    private AttendanceResponseDTO toDTO(Attendance attendance) {

        Employee emp = attendance.getEmployee();
        String fullName = emp.getFirstName() + " " + emp.getLastName();

        return AttendanceResponseDTO.builder()
                .attendanceId(attendance.getAttendanceId())
                .empId(emp.getEmpId())
                .employeeName(fullName)
                .date(attendance.getDate())
                .checkIn(attendance.getCheckIn())
                .checkOut(attendance.getCheckOut())
                .status(attendance.getStatus())
                .remarks(attendance.getRemarks())
                .build();
    }
}
