package com.example.hrms.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveTypeResponseDto {
    private Long leaveTypeId;
    private String name;
    private String description;
    private Integer maxDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
