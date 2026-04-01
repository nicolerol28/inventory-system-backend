package com.miapp.inventory_system.users.api.controller;

import com.miapp.inventory_system.users.api.dto.AuthResponse;
import com.miapp.inventory_system.users.api.dto.LoginRequest;
import com.miapp.inventory_system.users.api.dto.RegisterUserRequest;
import com.miapp.inventory_system.users.api.dto.UserResponse;
import com.miapp.inventory_system.users.api.mapper.UserApiMapper;
import com.miapp.inventory_system.users.application.usecase.LoginUseCase;
import com.miapp.inventory_system.users.application.usecase.RegisterUserUseCase;
import com.miapp.inventory_system.users.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final UserApiMapper mapper;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(loginUseCase.execute(request.email(), request.password()));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterUserRequest request) {

        User user = registerUserUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(user));
    }
}
