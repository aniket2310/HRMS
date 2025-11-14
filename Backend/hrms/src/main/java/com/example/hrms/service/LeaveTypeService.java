package com.example.hrms.service;

import com.example.hrms.dto.LeaveTypeRequestDto;
import com.example.hrms.dto.LeaveTypeResponseDto;
import com.example.hrms.entity.LeaveType;
import com.example.hrms.repository.LeaveTypeRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LeaveTypeService {

    private final LeaveTypeRepository repo;

    public LeaveTypeService(LeaveTypeRepository repo) {
        this.repo = repo;
    }

    public LeaveTypeResponseDto create(LeaveTypeRequestDto dto) {
        if (repo.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Leave type with this name already exists");
        }
        LeaveType lt = LeaveType.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .maxDays(dto.getMaxDays())
                .build();
        LeaveType saved = repo.save(lt);
        return toDto(saved);
    }

    public Optional<LeaveTypeResponseDto> getById(Long id) {
        return repo.findById(id).map(this::toDto);
    }

    public Page<LeaveTypeResponseDto> list(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return repo.findAll(pageable).map(this::toDto);
    }

    public Optional<LeaveTypeResponseDto> update(Long id, LeaveTypeRequestDto dto) {
        return repo.findById(id).map(existing -> {
            existing.setName(dto.getName());
            existing.setDescription(dto.getDescription());
            existing.setMaxDays(dto.getMaxDays());
            LeaveType updated = repo.save(existing);
            return toDto(updated);
        });
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private LeaveTypeResponseDto toDto(LeaveType lt) {
        return LeaveTypeResponseDto.builder()
                .leaveTypeId(lt.getLeaveTypeId())
                .name(lt.getName())
                .description(lt.getDescription())
                .maxDays(lt.getMaxDays())
                .createdAt(lt.getCreatedAt())
                .updatedAt(lt.getUpdatedAt())
                .build();
    }
}
