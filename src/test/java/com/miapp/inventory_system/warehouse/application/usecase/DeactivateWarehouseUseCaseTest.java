package com.miapp.inventory_system.warehouse.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.warehouse.domain.StockChecker;
import com.miapp.inventory_system.warehouse.domain.model.Warehouse;
import com.miapp.inventory_system.warehouse.domain.repository.WarehouseRepository;
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
class DeactivateWarehouseUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockChecker stockChecker;

    @InjectMocks
    private DeactivateWarehouseUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Long WAREHOUSE_ID = 5L;

    private Warehouse activeWarehouse() {
        return Warehouse.reconstitute(
                WAREHOUSE_ID, "Almacén Central", "Av. Principal 1", true, LocalDateTime.now());
    }

    private Warehouse inactiveWarehouse() {
        return Warehouse.reconstitute(
                WAREHOUSE_ID, "Almacén Central", "Av. Principal 1", false, LocalDateTime.now());
    }

    // =========================================================================
    // GROUP 1 — Happy path: almacén activo sin stock, se desactiva correctamente
    // =========================================================================

    @Test
    @DisplayName("execute deactivates warehouse when it exists and has no active stock")
    void should_deactivate_warehouse_successfully() {
        // given
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(activeWarehouse()));
        when(stockChecker.hasActiveStockByWarehouseId(WAREHOUSE_ID)).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(WAREHOUSE_ID);

        // then — el objeto pasado a save debe tener active = false
        ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    // =========================================================================
    // GROUP 2 — Almacén no encontrado
    // =========================================================================

    @Test
    @DisplayName("execute throws ResourceNotFoundException when warehouse does not exist")
    void should_throw_when_warehouse_not_found() {
        // given
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(WAREHOUSE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(WAREHOUSE_ID));

        verify(stockChecker, never()).hasActiveStockByWarehouseId(any());
        verify(warehouseRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 3 — Almacén con stock activo: no se puede desactivar
    // =========================================================================

    @Test
    @DisplayName("execute throws when warehouse has active stock associated")
    void should_throw_when_warehouse_has_active_stock() {
        // given
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(activeWarehouse()));
        when(stockChecker.hasActiveStockByWarehouseId(WAREHOUSE_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(WAREHOUSE_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock activo");

        verify(warehouseRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 4 — Cortocircuito: si no existe, no se consulta stock ni save
    // =========================================================================

    @Test
    @DisplayName("execute never checks stock or saves when warehouse is not found")
    void should_not_check_stock_or_save_when_warehouse_not_found() {
        // given
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> useCase.execute(WAREHOUSE_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(stockChecker, never()).hasActiveStockByWarehouseId(any());
        verify(warehouseRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 5 — Almacén ya inactivo: domain lanza excepción antes de persistir
    // =========================================================================

    @Test
    @DisplayName("execute throws when warehouse is already inactive")
    void should_throw_when_warehouse_is_already_inactive() {
        // given — almacén reconstituido con active=false
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(inactiveWarehouse()));
        when(stockChecker.hasActiveStockByWarehouseId(WAREHOUSE_ID)).thenReturn(false);

        // when / then — el dominio lanza la excepción en warehouse.deactivate()
        assertThatThrownBy(() -> useCase.execute(WAREHOUSE_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desactivado");

        verify(warehouseRepository, never()).save(any());
    }
}
