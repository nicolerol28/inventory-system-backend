package com.miapp.inventory_system.inventory.application.usecase;

import com.miapp.inventory_system.inventory.application.command.InitializeStockCommand;
import com.miapp.inventory_system.inventory.domain.model.Stock;
import com.miapp.inventory_system.inventory.domain.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InitializeStockUseCase {
    private final StockRepository stockRepository;

    @Transactional
    public Stock execute(InitializeStockCommand command) {

        // Verificar que no existe ya un stock para ese producto en esa sede
        stockRepository
                .findByProductIdAndWarehouseId(
                        command.productId(),
                        command.warehouseId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Ya existe stock inicializado para este producto en esta sede");
                });

        // Crear el stock en cero, el producto queda autorizado en la sede
        Stock stock = Stock.create(
                command.productId(),
                command.warehouseId(),
                command.minQuantity()
        );

        return stockRepository.save(stock);
    }
}
