package com.miapp.inventory_system.inventory.application.usecase;

import com.miapp.inventory_system.inventory.domain.model.Stock;
import com.miapp.inventory_system.inventory.domain.repository.StockRepository;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UpdateMinQuantityUseCase {

    private final StockRepository stockRepository;

    @Transactional
    public Stock execute(Long stockId, BigDecimal minQuantity) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe stock con id: " + stockId));

        stock.updateMinQuantity(minQuantity);

        return stockRepository.save(stock);
    }
}
