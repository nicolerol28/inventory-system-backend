package com.miapp.inventory_system.users.api.controller;

import com.miapp.inventory_system.shared.dto.PageResponse;
import com.miapp.inventory_system.users.api.dto.UpdateUserRequest;
import com.miapp.inventory_system.users.api.dto.UserResponse;
import com.miapp.inventory_system.users.api.mapper.UserApiMapper;
import com.miapp.inventory_system.users.application.query.UserQueryService;
import com.miapp.inventory_system.users.application.usecase.DeactivateUserUseCase;
import com.miapp.inventory_system.users.application.usecase.UpdateUserUseCase;
import com.miapp.inventory_system.users.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UpdateUserUseCase updateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final UserQueryService userQueryService;
    private final UserApiMapper mapper;

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        User user = updateUserUseCase.execute(mapper.toCommand(request, id));
        return ResponseEntity.ok(mapper.toResponse(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userQueryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(userQueryService.getAll(page, size));
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getAllActive() {
        return ResponseEntity.ok(userQueryService.getAllActive());
    }
}