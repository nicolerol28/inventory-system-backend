package com.miapp.inventory_system.warehouse.application.usecase;

import com.miapp.inventory_system.warehouse.application.command.RegisterWarehouseCommand;
import com.miapp.inventory_system.warehouse.domain.model.Warehouse;
import com.miapp.inventory_system.warehouse.domain.repository.WarehouseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterWarehouseUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private RegisterWarehouseUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final String WAREHOUSE_NAME     = "Almacén Central";
    private static final String WAREHOUSE_LOCATION = "Av. Siempre Viva 123";

    private RegisterWarehouseCommand buildCommand() {
        return new RegisterWarehouseCommand(WAREHOUSE_NAME, WAREHOUSE_LOCATION);
    }

    // =========================================================================
    // GROUP 1 — Happy path: almacén guardado correctamente
    // =========================================================================

    @Test
    @DisplayName("execute registers warehouse successfully when name is not duplicated")
    void should_register_warehouse_successfully() {
        // given
        when(warehouseRepository.existsByName(WAREHOUSE_NAME)).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> {
            Warehouse w = inv.getArgument(0);
            return Warehouse.reconstitute(1L, w.getName(), w.getLocation(), w.isActive(), w.getCreatedAt());
        });

        // when
        Warehouse result = useCase.execute(buildCommand());

        // then
        assertThat(result.getName()).isEqualTo(WAREHOUSE_NAME);
        assertThat(result.getLocation()).isEqualTo(WAREHOUSE_LOCATION);
        assertThat(result.isActive()).isTrue();
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    @DisplayName("execute registers warehouse successfully when location is null")
    void should_register_warehouse_successfully_with_null_location() {
        // given
        RegisterWarehouseCommand command = new RegisterWarehouseCommand(WAREHOUSE_NAME, null);
        when(warehouseRepository.existsByName(WAREHOUSE_NAME)).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Warehouse result = useCase.execute(command);

        // then
        assertThat(result.getLocation()).isNull();
        assertThat(result.isActive()).isTrue();
    }

    // =========================================================================
    // GROUP 2 — Nombre duplicado: lanza excepción
    // =========================================================================

    @Test
    @DisplayName("execute throws when a warehouse with the same name already exists")
    void should_throw_when_name_already_exists() {
        // given
        when(warehouseRepository.existsByName(WAREHOUSE_NAME)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(WAREHOUSE_NAME);
    }

    // =========================================================================
    // GROUP 3 — Cortocircuito: nombre duplicado => save nunca se llama
    // =========================================================================

    @Test
    @DisplayName("execute never calls save when name conflict is detected")
    void should_never_call_save_when_name_is_duplicated() {
        // given
        when(warehouseRepository.existsByName(WAREHOUSE_NAME)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(warehouseRepository, never()).save(any());
    }
}
