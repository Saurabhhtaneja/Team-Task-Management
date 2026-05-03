package com.example.team_task_manager.service;


import com.example.team_task_manager.dto.LoginRequest;
import com.example.team_task_manager.dto.SignupRequest;
import com.example.team_task_manager.dto.JwtResponse;
import com.example.team_task_manager.dto.MessageResponse;
import com.example.team_task_manager.entity.Role;
import com.example.team_task_manager.entity.Role.ERole;
import com.example.team_task_manager.entity.User;
import com.example.team_task_manager.exception.BadRequestException;
import com.example.team_task_manager.repository.RoleRepository;
import com.example.team_task_manager.repository.UserRepository;
import com.example.team_task_manager.security.JwtUtils;
import com.example.team_task_manager.security.UserDetailsImpl;
import com.example.team_task_manager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), userDetails.getFullName(), roles);
    }

    @Override
    @Transactional
    public MessageResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        ERole erole = ERole.ROLE_MEMBER;
        if ("ROLE_ADMIN".equalsIgnoreCase(request.getRole())) {
            erole = ERole.ROLE_ADMIN;
        }

        Role role = roleRepository.findByName(erole)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(role))
                .build();

        userRepository.save(user);
        return new MessageResponse("User registered successfully");
    }
}