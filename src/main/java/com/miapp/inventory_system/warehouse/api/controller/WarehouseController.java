package com.miapp.inventory_system.warehouse.api.controller;

import com.miapp.inventory_system.shared.dto.PageResponse;
import com.miapp.inventory_system.warehouse.api.dto.RegisterWarehouseRequest;
import com.miapp.inventory_system.warehouse.api.dto.UpdateWarehouseRequest;
import com.miapp.inventory_system.warehouse.api.dto.WarehouseResponse;
import com.miapp.inventory_system.warehouse.api.mapper.WarehouseApiMapper;
import com.miapp.inventory_system.warehouse.application.query.WarehouseQueryService;
import com.miapp.inventory_system.warehouse.application.usecase.DeactivateWarehouseUseCase;
import com.miapp.inventory_system.warehouse.application.usecase.RegisterWarehouseUseCase;
import com.miapp.inventory_system.warehouse.application.usecase.UpdateWarehouseUseCase;
import com.miapp.inventory_system.warehouse.domain.model.Warehouse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final RegisterWarehouseUseCase registerWarehouseUseCase;
    private final UpdateWarehouseUseCase updateWarehouseUseCase;
    private final DeactivateWarehouseUseCase deactivateWarehouseUseCase;
    private final WarehouseQueryService warehouseQueryService;
    private final WarehouseApiMapper mapper;

    @PostMapping
    public ResponseEntity<WarehouseResponse> register(
            @Valid @RequestBody RegisterWarehouseRequest request) {

        Warehouse warehouse = registerWarehouseUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(warehouse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWarehouseRequest request) {

        Warehouse warehouse = updateWarehouseUseCase.execute(mapper.toCommand(request, id));
        return ResponseEntity.ok(mapper.toResponse(warehouse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateWarehouseUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseQueryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<WarehouseResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "all") String filterActive) {

        return ResponseEntity.ok(warehouseQueryService.getAll(page, size, name, filterActive));
    }

    @GetMapping("/active")
    public ResponseEntity<List<WarehouseResponse>> getActive() {
        return ResponseEntity.ok(warehouseQueryService.getAllActive());
    }
}
