package com.example.hrms.controller;

import com.example.hrms.dto.AttendanceRequestDTO;
import com.example.hrms.dto.AttendanceResponseDTO;
import com.example.hrms.service.AttendanceService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ------------------------------------------------------
    // CHECK IN
    // ------------------------------------------------------
    @PostMapping("/check-in/{empId}")
    public ResponseEntity<AttendanceResponseDTO> checkIn(@PathVariable Long empId) {
        AttendanceResponseDTO response = attendanceService.checkIn(empId);
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------
    // CHECK OUT
    // ------------------------------------------------------
    @PutMapping("/check-out/{empId}")
    public ResponseEntity<AttendanceResponseDTO> checkOut(@PathVariable Long empId) {
        AttendanceResponseDTO response = attendanceService.checkOut(empId);
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------
    // GET ATTENDANCE - SPECIFIC EMPLOYEE
    // ------------------------------------------------------
    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<AttendanceResponseDTO>> getAttendanceByEmployee(@PathVariable Long empId) {
        List<AttendanceResponseDTO> list = attendanceService.getAttendanceByEmployee(empId);
        return ResponseEntity.ok(list);
    }

    // ------------------------------------------------------
    // GET ALL ATTENDANCE
    // (Admin / HR)
    // ------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<AttendanceResponseDTO>> getAllAttendance() {
        List<AttendanceResponseDTO> list = attendanceService.getAllAttendance();
        return ResponseEntity.ok(list);
    }

    // ------------------------------------------------------
    // GET ATTENDANCE BY DATE
    // Example: /api/attendance/date/2025-01-10
    // ------------------------------------------------------
    @GetMapping("/date/{date}")
    public ResponseEntity<List<AttendanceResponseDTO>> getAttendanceByDate(@PathVariable String date) {
        List<AttendanceResponseDTO> list = attendanceService.getAttendanceByDate(date);
        return ResponseEntity.ok(list);
    }

    // ------------------------------------------------------
    // UPDATE STATUS (HR / ADMIN)
    // Example: PATCH /api/attendance/20
    // Body: { "status": "Leave", "remarks": "Approved leave" }
    // ------------------------------------------------------
    @PatchMapping("/{attendanceId}")
    public ResponseEntity<AttendanceResponseDTO> updateStatus(
            @PathVariable Long attendanceId,
            @RequestBody AttendanceRequestDTO dto) {

        AttendanceResponseDTO response = attendanceService.updateStatus(attendanceId, dto);
        return ResponseEntity.ok(response);
    }
}
