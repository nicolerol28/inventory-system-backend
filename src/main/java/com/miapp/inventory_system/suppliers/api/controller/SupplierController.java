package com.miapp.inventory_system.suppliers.api.controller;

import com.miapp.inventory_system.shared.dto.PageResponse;
import com.miapp.inventory_system.suppliers.api.dto.RegisterSupplierRequest;
import com.miapp.inventory_system.suppliers.api.dto.SupplierResponse;
import com.miapp.inventory_system.suppliers.api.dto.UpdateSupplierRequest;
import com.miapp.inventory_system.suppliers.api.mapper.SupplierApiMapper;
import com.miapp.inventory_system.suppliers.application.query.SupplierQueryService;
import com.miapp.inventory_system.suppliers.application.usecase.DeactivateSupplierUseCase;
import com.miapp.inventory_system.suppliers.application.usecase.RegisterSupplierUseCase;
import com.miapp.inventory_system.suppliers.application.usecase.UpdateSupplierUseCase;
import com.miapp.inventory_system.suppliers.domain.model.Supplier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final RegisterSupplierUseCase registerSupplierUseCase;
    private final UpdateSupplierUseCase updateSupplierUseCase;
    private final DeactivateSupplierUseCase deactivateSupplierUseCase;
    private final SupplierQueryService supplierQueryService;
    private final SupplierApiMapper mapper;

    @PostMapping
    public ResponseEntity<SupplierResponse> register(
            @Valid @RequestBody RegisterSupplierRequest request) {

        Supplier supplier = registerSupplierUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(supplier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierRequest request) {

        Supplier supplier = updateSupplierUseCase.execute(mapper.toCommand(request, id));
        return ResponseEntity.ok(mapper.toResponse(supplier));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateSupplierUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierQueryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<SupplierResponse>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(supplierQueryService.getAll(page, size));
    }

    @GetMapping("/active")
    public ResponseEntity<List<SupplierResponse>> getAllActive() {
        return ResponseEntity.ok(supplierQueryService.getAllActive());
    }
}

