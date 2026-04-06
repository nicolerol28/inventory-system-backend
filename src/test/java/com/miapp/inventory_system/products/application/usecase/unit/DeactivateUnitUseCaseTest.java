package com.miapp.inventory_system.products.application.usecase.unit;

import com.miapp.inventory_system.products.domain.model.Unit;
import com.miapp.inventory_system.products.domain.repository.ProductRepository;
import com.miapp.inventory_system.products.domain.repository.UnitRepository;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeactivateUnitUseCaseTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DeactivateUnitUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Long UNIT_ID = 4L;

    private Unit activeUnit() {
        return Unit.reconstitute(UNIT_ID, "Kilogramo", "kg", true, LocalDateTime.now());
    }

    private Unit inactiveUnit() {
        return Unit.reconstitute(UNIT_ID, "Kilogramo", "kg", false, LocalDateTime.now());
    }

    // =========================================================================
    // GROUP 1 — Happy path: unidad activa sin productos activos, se desactiva
    // =========================================================================

    @Test
    @DisplayName("execute deactivates unit when it exists and has no active products")
    void should_deactivate_unit_successfully() {
        // given
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(activeUnit()));
        when(productRepository.existsActiveByUnitId(UNIT_ID)).thenReturn(false);
        when(unitRepository.save(any(Unit.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(UNIT_ID);

        // then — el objeto pasado a save debe tener active = false
        ArgumentCaptor<Unit> captor = ArgumentCaptor.forClass(Unit.class);
        verify(unitRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    // =========================================================================
    // GROUP 2 — Unidad no encontrada
    // =========================================================================

    @Test
    @DisplayName("execute throws ResourceNotFoundException when unit does not exist")
    void should_throw_when_unit_not_found() {
        // given
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(UNIT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(UNIT_ID));

        verify(productRepository, never()).existsActiveByUnitId(any());
        verify(unitRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 3 — Unidad con productos activos: no se puede desactivar
    // =========================================================================

    @Test
    @DisplayName("execute throws when unit has active products associated")
    void should_throw_when_unit_has_active_products() {
        // given
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(activeUnit()));
        when(productRepository.existsActiveByUnitId(UNIT_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(UNIT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productos activos");

        verify(unitRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 4 — Cortocircuito: si no existe, no se consultan productos ni save
    // =========================================================================

    @Test
    @DisplayName("execute never checks active products or saves when unit is not found")
    void should_not_check_products_or_save_when_unit_not_found() {
        // given
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> useCase.execute(UNIT_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(productRepository, never()).existsActiveByUnitId(any());
        verify(unitRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 5 — Unidad ya inactiva: domain lanza excepción antes de persistir
    // =========================================================================

    @Test
    @DisplayName("execute throws when unit is already inactive")
    void should_throw_when_unit_is_already_inactive() {
        // given — unidad reconstituida con active=false
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(inactiveUnit()));
        when(productRepository.existsActiveByUnitId(UNIT_ID)).thenReturn(false);

        // when / then — el dominio lanza la excepción en unit.deactivate()
        assertThatThrownBy(() -> useCase.execute(UNIT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desactivada");

        verify(unitRepository, never()).save(any());
    }
}
