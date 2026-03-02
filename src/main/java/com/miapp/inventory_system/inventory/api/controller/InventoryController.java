package com.miapp.inventory_system.inventory.api.controller;

import com.miapp.inventory_system.inventory.api.dto.InventoryMovementResponse;
import com.miapp.inventory_system.inventory.api.dto.RegisterStockMovementRequest;
import com.miapp.inventory_system.inventory.api.mapper.InventoryApiMapper;
import com.miapp.inventory_system.inventory.application.command.RegisterStockMovementCommand;
import com.miapp.inventory_system.inventory.application.usecase.RegisterStockMovementUseCase;
import com.miapp.inventory_system.inventory.domain.model.InventoryMovement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final RegisterStockMovementUseCase registerStockMovementUseCase;
    private final InventoryApiMapper mapper;

    @PostMapping("/movements")
    public ResponseEntity<InventoryMovementResponse> registerMovement(
            @RequestBody RegisterStockMovementRequest request) {

        RegisterStockMovementCommand command = mapper.toCommand(request);
        InventoryMovement movement = registerStockMovementUseCase.execute(command);
        InventoryMovementResponse response = mapper.toResponse(movement);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}