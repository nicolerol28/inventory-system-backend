package com.miapp.inventory_system.users.api.controller;

import com.miapp.inventory_system.shared.security.JwtService;
import com.miapp.inventory_system.users.api.dto.AuthResponse;
import com.miapp.inventory_system.users.api.dto.LoginRequest;
import com.miapp.inventory_system.users.api.dto.RegisterUserRequest;
import com.miapp.inventory_system.users.api.dto.UserResponse;
import com.miapp.inventory_system.users.api.mapper.UserApiMapper;
import com.miapp.inventory_system.users.application.usecase.RegisterUserUseCase;
import com.miapp.inventory_system.users.domain.model.User;
import com.miapp.inventory_system.users.infrastructure.entity.UserJpaEntity;
import com.miapp.inventory_system.users.infrastructure.repository.UserJpaRepositorySpring;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserJpaRepositorySpring userJpaRepository;
    private final RegisterUserUseCase registerUserUseCase;
    private final UserApiMapper mapper;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserJpaEntity user = userJpaRepository.findByEmail(request.email())
                .orElseThrow();

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(new AuthResponse(
                token,
                user.getId(),
                user.getRole(),
                jwtService.extractExpiresAt(token)
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterUserRequest request) {

        User user = registerUserUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(user));
    }
}