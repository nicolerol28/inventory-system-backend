package com.miapp.inventory_system.products.api.controller;

import com.miapp.inventory_system.products.api.dto.unit.RegisterUnitRequest;
import com.miapp.inventory_system.products.api.dto.unit.UnitResponse;
import com.miapp.inventory_system.products.api.dto.unit.UpdateUnitRequest;
import com.miapp.inventory_system.products.api.mapper.UnitApiMapper;
import com.miapp.inventory_system.products.application.query.UnitQueryService;
import com.miapp.inventory_system.products.application.usecase.unit.DeactivateUnitUseCase;
import com.miapp.inventory_system.products.application.usecase.unit.RegisterUnitUseCase;
import com.miapp.inventory_system.products.application.usecase.unit.UpdateUnitUseCase;
import com.miapp.inventory_system.products.domain.model.Unit;
import com.miapp.inventory_system.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
public class UnitController {

    private final RegisterUnitUseCase registerUnitUseCase;
    private final UpdateUnitUseCase updateUnitUseCase;
    private final DeactivateUnitUseCase deactivateUnitUseCase;
    private final UnitQueryService unitQueryService;
    private final UnitApiMapper mapper;

    @PostMapping
    public ResponseEntity<UnitResponse> register(
            @Valid @RequestBody RegisterUnitRequest request) {

        Unit unit = registerUnitUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(unit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnitResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUnitRequest request) {

        Unit unit = updateUnitUseCase.execute(mapper.toCommand(request, id));
        return ResponseEntity.ok(mapper.toResponse(unit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateUnitUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnitResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(unitQueryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<UnitResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "all") String filterActive,
            @RequestParam(defaultValue = "asc") String sortName) {

        return ResponseEntity.ok(unitQueryService.getAll(page, size, name, filterActive, sortName));
    }

    @GetMapping("/active")
    public ResponseEntity<List<UnitResponse>> getAllActive() {
        return ResponseEntity.ok(unitQueryService.getAllActive());
    }
}