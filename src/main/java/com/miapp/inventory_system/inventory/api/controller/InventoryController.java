package com.miapp.inventory_system.inventory.api.controller;

import com.miapp.inventory_system.inventory.api.dto.*;
import com.miapp.inventory_system.inventory.api.mapper.InventoryApiMapper;
import com.miapp.inventory_system.inventory.application.command.InitializeStockCommand;
import com.miapp.inventory_system.inventory.application.command.RegisterStockMovementCommand;
import com.miapp.inventory_system.inventory.application.query.InventoryQueryService;
import com.miapp.inventory_system.inventory.application.usecase.InitializeStockUseCase;
import com.miapp.inventory_system.inventory.application.usecase.RegisterStockMovementUseCase;
import com.miapp.inventory_system.inventory.domain.model.InventoryMovement;
import com.miapp.inventory_system.inventory.domain.model.Stock;
import com.miapp.inventory_system.shared.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final RegisterStockMovementUseCase registerStockMovementUseCase;
    private final InventoryApiMapper mapper;
    private final InventoryQueryService queryService;
    private final InitializeStockUseCase initializeStockUseCase;

    @PostMapping("/movements")
    public ResponseEntity<InventoryMovementResponse> registerMovement(
            @Valid  @RequestBody RegisterStockMovementRequest request) {

        RegisterStockMovementCommand command = mapper.toCommand(request);
        InventoryMovement movement = registerStockMovementUseCase.execute(command);
        InventoryMovementResponse response = mapper.toResponse(movement);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/stock/initialize")
    public ResponseEntity<StockResponse> initializeStock(
            @Valid @RequestBody InitializeStockRequest request) {

        InitializeStockCommand command = new InitializeStockCommand(
                request.productId(),
                request.warehouseId(),
                request.minQuantity()
        );

        Stock stock = initializeStockUseCase.execute(command);

        StockResponse response = new StockResponse(
                stock.getId(),
                stock.getProductId(),
                "Producto #" + stock.getProductId(),
                stock.getWarehouseId(),
                stock.getQuantity(),
                stock.getMinQuantity(),
                stock.isBelowMinimum(),
                stock.getUpdatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/stock")
    public ResponseEntity<StockResponse> getStock(
            @RequestParam Long productId,
            @RequestParam Long warehouseId) {

        return ResponseEntity.ok(queryService.getStock(productId, warehouseId));
    }

    @GetMapping("/movements")
    public ResponseEntity<PageResponse<InventoryMovementResponse>> getMovements(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                queryService.getMovements(productId, warehouseId, page, size));
    }



    @GetMapping("/stock/warehouse/{warehouseId}")
    public ResponseEntity<PageResponse<StockResponse>> getStockByWarehouse(
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                queryService.getStockByWarehouse(warehouseId, page, size));
    }

    @GetMapping("/movements/warehouse/{warehouseId}")
    public ResponseEntity<PageResponse<InventoryMovementResponse>> getMovementsByWarehouse(
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                queryService.getMovementsByWarehouse(warehouseId, page, size));
    }

    @GetMapping("/movements/warehouse/{warehouseId}/date-range")
    public ResponseEntity<PageResponse<InventoryMovementResponse>> getMovementsByDateRange(
            @PathVariable Long warehouseId,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                queryService.getMovementsByDateRange(warehouseId, from, to, page, size));
    }


}