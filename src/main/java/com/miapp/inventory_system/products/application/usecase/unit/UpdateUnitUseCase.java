package com.miapp.inventory_system.products.application.usecase.unit;

import com.miapp.inventory_system.products.application.command.unit.UpdateUnitCommand;
import com.miapp.inventory_system.products.domain.model.Unit;
import com.miapp.inventory_system.products.domain.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUnitUseCase {

    private final UnitRepository unitRepository;

    @Transactional
    public Unit execute(UpdateUnitCommand command) {

        Unit unit = unitRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una unidad con id: " + command.id()));

        if (unitRepository.existsByNameAndIdNot(command.name(), command.id())) {
            throw new IllegalArgumentException(
                    "Ya existe otra unidad con el nombre: " + command.name());
        }

        if (unitRepository.existsBySymbolAndIdNot(command.symbol(), command.id())) {
            throw new IllegalArgumentException(
                    "Ya existe otra unidad con el símbolo: " + command.symbol());
        }

        unit.update(command.name(), command.symbol());
        return unitRepository.save(unit);
    }
}