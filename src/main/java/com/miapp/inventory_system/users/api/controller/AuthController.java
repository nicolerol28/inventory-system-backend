package com.miapp.inventory_system.users.api.controller;

import com.miapp.inventory_system.users.api.dto.AuthResponse;
import com.miapp.inventory_system.users.api.dto.GoogleLoginRequest;
import com.miapp.inventory_system.users.api.dto.LoginRequest;
import com.miapp.inventory_system.users.api.dto.RegisterUserRequest;
import com.miapp.inventory_system.users.api.dto.UserResponse;
import com.miapp.inventory_system.users.api.mapper.UserApiMapper;
import com.miapp.inventory_system.users.application.RegisterUserResult;
import com.miapp.inventory_system.users.application.usecase.GoogleLoginUseCase;
import com.miapp.inventory_system.users.application.usecase.LoginUseCase;
import com.miapp.inventory_system.users.application.usecase.RegisterUserUseCase;
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
    private final GoogleLoginUseCase googleLoginUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final UserApiMapper mapper;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        var result = loginUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(new AuthResponse(
                result.token(),
                result.userId(),
                result.role(),
                result.expiresAt()
        ));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        var result = googleLoginUseCase.execute(request.idToken());
        return ResponseEntity.ok(new AuthResponse(
                result.token(),
                result.userId(),
                result.role(),
                result.expiresAt()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterUserRequest request) {

        RegisterUserResult result = registerUserUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse(result.userId(), result.name(), result.email(), result.role(), result.active(), null, null));
    }
}
