package com.miapp.inventory_system.products.application.usecase.unit;

import com.miapp.inventory_system.products.domain.model.Unit;
import com.miapp.inventory_system.products.domain.repository.ProductRepository;
import com.miapp.inventory_system.products.domain.repository.UnitRepository;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivateUnitUseCase {

    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void execute(Long id) {

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una unidad con id: " + id));

        if (productRepository.existsActiveByUnitId(id)) {
            throw new IllegalArgumentException(
                    "No se puede desactivar una unidad que tiene productos activos asociados");
        }

        unit.deactivate();
        unitRepository.save(unit);
    }
}