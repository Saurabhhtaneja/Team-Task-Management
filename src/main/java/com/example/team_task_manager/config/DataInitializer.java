package com.example.team_task_manager.config;


import com.example.team_task_manager.entity.Role;
import com.example.team_task_manager.entity.Role.ERole;
import com.example.team_task_manager.entity.User;
import com.example.team_task_manager.repository.RoleRepository;
import com.example.team_task_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed roles
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, ERole.ROLE_ADMIN));
            roleRepository.save(new Role(null, ERole.ROLE_MEMBER));
            log.info("Roles seeded.");
        }

        // Seed default admin
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User admin = User.builder()
                    .username("admin")
                    .email("admin@taskmanager.com")
                    .fullName("System Admin")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(admin);
            log.info("Default admin created — username: admin, password: admin123");
        }
    }
}
