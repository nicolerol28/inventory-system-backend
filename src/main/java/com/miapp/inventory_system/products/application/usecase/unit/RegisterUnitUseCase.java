package com.miapp.inventory_system.products.application.usecase.unit;

import com.miapp.inventory_system.products.application.command.unit.RegisterUnitCommand;
import com.miapp.inventory_system.products.domain.model.Unit;
import com.miapp.inventory_system.products.domain.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUnitUseCase {

    private final UnitRepository unitRepository;

    @Transactional
    public Unit execute(RegisterUnitCommand command) {

        if (unitRepository.existsByName(command.name())) {
            throw new IllegalArgumentException(
                    "Ya existe una unidad con el nombre: " + command.name());
        }

        if (unitRepository.existsBySymbol(command.symbol())) {
            throw new IllegalArgumentException(
                    "Ya existe una unidad con el símbolo: " + command.symbol());
        }

        Unit unit = Unit.create(command.name(), command.symbol());
        return unitRepository.save(unit);
    }
}