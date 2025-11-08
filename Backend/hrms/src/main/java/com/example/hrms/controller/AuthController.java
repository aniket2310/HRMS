package com.example.hrms.controller;

import com.example.hrms.dto.AuthRequestDto;
import com.example.hrms.dto.JwtResponseDto;
import com.example.hrms.dto.RegisterUserDto;
import com.example.hrms.entity.Role;
import com.example.hrms.entity.User;
import com.example.hrms.repository.RoleRepository;
import com.example.hrms.repository.UserRepository;
import com.example.hrms.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Authentication APIs", description = "Register, Login, Profile")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager, JwtTokenProvider jwtProvider,
                          UserRepository userRepository, RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDto dto, Authentication authentication) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already taken!"));
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered!"));
        }

        // Always make sure EMPLOYEE exists
        Role employee = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> roleRepository.save(Role.builder().name("EMPLOYEE").build()));

        // Decide roles to assign
        Set<Role> rolesToAssign;
        boolean callerIsAdmin = authentication != null &&
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (callerIsAdmin && dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            // ADMIN may assign requested roles (validate all exist)
            try {
                rolesToAssign = dto.getRoles().stream()
                        .map(String::toUpperCase)
                        .map(name -> roleRepository.findByName(name).orElseThrow())
                        .collect(Collectors.toSet());
            } catch (NoSuchElementException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "One or more roles not found"));
            }
        } else {
            // Public/self registration OR non-admin -> force EMPLOYEE
            rolesToAssign = Collections.singleton(employee);
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .active(true)
                .roles(rolesToAssign)
                .build();

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody AuthRequestDto request) {
        var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String token = jwtProvider.generateToken(auth);
        return ResponseEntity.ok(
                JwtResponseDto.builder()
                        .token(token)
                        .username(request.getUsername())
                        .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).build();
        var userOpt = userRepository.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(404).build();
        var u = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "username", u.getUsername(),
                "email", u.getEmail(),
                "roles", u.getRoles().stream().map(Role::getName).toList()
        ));
    }
}
