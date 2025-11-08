package com.example.hrms.config;

import com.example.hrms.entity.Role;
import com.example.hrms.entity.User;
import com.example.hrms.repository.RoleRepository;
import com.example.hrms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Configuration
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.initial.admin.username:admin}")
    private String initialAdminUsername;

    @Value("${app.initial.admin.email:admin@example.com}")
    private String initialAdminEmail;

    @Value("${app.initial.admin.password:Admin@123}")
    private String initialAdminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Ensure base roles exist
        Role admin = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));
        roleRepository.findByName("HR")
                .orElseGet(() -> roleRepository.save(Role.builder().name("HR").build()));
        roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> roleRepository.save(Role.builder().name("EMPLOYEE").build()));

        // Create first admin only if there are no users
        if (userRepository.count() == 0) {
            User user = User.builder()
                    .username(initialAdminUsername)
                    .email(initialAdminEmail)
                    .password(passwordEncoder.encode(initialAdminPassword))
                    .active(true)
                    .roles(Set.of(admin))
                    .build();
            userRepository.save(user);
            System.out.println("👉 Initial admin created: " + initialAdminUsername + " / " + initialAdminEmail);
        }
    }
}
