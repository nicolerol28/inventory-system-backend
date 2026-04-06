package com.miapp.inventory_system.warehouse.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.warehouse.application.command.UpdateWarehouseCommand;
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
class UpdateWarehouseUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private UpdateWarehouseUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Long   WAREHOUSE_ID       = 2L;
    private static final String WAREHOUSE_NAME     = "Almacén Norte";
    private static final String WAREHOUSE_LOCATION = "Ruta 9 km 50";

    private UpdateWarehouseCommand buildCommand() {
        return new UpdateWarehouseCommand(WAREHOUSE_ID, WAREHOUSE_NAME, WAREHOUSE_LOCATION);
    }

    private Warehouse existingWarehouse() {
        return Warehouse.reconstitute(
                WAREHOUSE_ID, "Nombre anterior", "Ubicación anterior", true, LocalDateTime.now());
    }

    private void mockSavePassThrough() {
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // =========================================================================
    // GROUP 1 — Happy path: nombre y location actualizados, save llamado una vez
    // =========================================================================

    @Test
    @DisplayName("execute updates warehouse name and location and calls save once")
    void should_update_warehouse_successfully() {
        // given
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(existingWarehouse()));
        when(warehouseRepository.existsByNameAndIdNot(WAREHOUSE_NAME, WAREHOUSE_ID)).thenReturn(false);
        mockSavePassThrough();

        ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);

        // when
        Warehouse result = useCase.execute(buildCommand());

        // then
        verify(warehouseRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(WAREHOUSE_NAME);
        assertThat(captor.getValue().getLocation()).isEqualTo(WAREHOUSE_LOCATION);
        assertThat(result.getName()).isEqualTo(WAREHOUSE_NAME);
        assertThat(result.getLocation()).isEqualTo(WAREHOUSE_LOCATION);
    }

    @Test
    @DisplayName("execute updates warehouse successfully when location is null")
    void should_update_warehouse_successfully_with_null_location() {
        // given
        UpdateWarehouseCommand command = new UpdateWarehouseCommand(WAREHOUSE_ID, WAREHOUSE_NAME, null);
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(existingWarehouse()));
        when(warehouseRepository.existsByNameAndIdNot(WAREHOUSE_NAME, WAREHOUSE_ID)).thenReturn(false);
        mockSavePassThrough();

        // when
        Warehouse result = useCase.execute(command);

        // then
        assertThat(result.getLocation()).isNull();
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
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(WAREHOUSE_ID));

        verify(warehouseRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 3 — Nombre duplicado en otro almacén
    // =========================================================================

    @Test
    @DisplayName("execute throws when another warehouse already has the same name")
    void should_throw_when_name_belongs_to_another_warehouse() {
        // given
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(existingWarehouse()));
        when(warehouseRepository.existsByNameAndIdNot(WAREHOUSE_NAME, WAREHOUSE_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(WAREHOUSE_NAME);

        verify(warehouseRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 4 — Cortocircuito: nombre duplicado => save nunca se llama
    // =========================================================================

    @Test
    @DisplayName("execute never calls save when name conflict is detected")
    void should_never_call_save_when_name_conflict_detected() {
        // given
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(existingWarehouse()));
        when(warehouseRepository.existsByNameAndIdNot(WAREHOUSE_NAME, WAREHOUSE_ID)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(warehouseRepository, never()).save(any());
    }
}
