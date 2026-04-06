package com.miapp.inventory_system.products.application.usecase.unit;

import com.miapp.inventory_system.products.application.command.unit.RegisterUnitCommand;
import com.miapp.inventory_system.products.domain.model.Unit;
import com.miapp.inventory_system.products.domain.repository.UnitRepository;
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
class RegisterUnitUseCaseTest {

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private RegisterUnitUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final String UNIT_NAME   = "Kilogramo";
    private static final String UNIT_SYMBOL = "kg";

    private RegisterUnitCommand buildCommand() {
        return new RegisterUnitCommand(UNIT_NAME, UNIT_SYMBOL);
    }

    // =========================================================================
    // GROUP 1 — Happy path: unidad guardada correctamente
    // =========================================================================

    @Test
    @DisplayName("execute registers unit successfully when name and symbol are not duplicated")
    void should_register_unit_successfully() {
        // given
        when(unitRepository.existsByName(UNIT_NAME)).thenReturn(false);
        when(unitRepository.existsBySymbol(UNIT_SYMBOL)).thenReturn(false);
        when(unitRepository.save(any(Unit.class))).thenAnswer(inv -> {
            Unit u = inv.getArgument(0);
            return Unit.reconstitute(1L, u.getName(), u.getSymbol(), u.isActive(), u.getCreatedAt());
        });

        // when
        Unit result = useCase.execute(buildCommand());

        // then
        assertThat(result.getName()).isEqualTo(UNIT_NAME);
        assertThat(result.getSymbol()).isEqualTo(UNIT_SYMBOL);
        assertThat(result.isActive()).isTrue();
        verify(unitRepository, times(1)).save(any(Unit.class));
    }

    // =========================================================================
    // GROUP 2 — Nombre duplicado: lanza excepción
    // =========================================================================

    @Test
    @DisplayName("execute throws when a unit with the same name already exists")
    void should_throw_when_name_already_exists() {
        // given
        when(unitRepository.existsByName(UNIT_NAME)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNIT_NAME);
    }

    // =========================================================================
    // GROUP 3 — Símbolo duplicado: lanza excepción
    // =========================================================================

    @Test
    @DisplayName("execute throws when a unit with the same symbol already exists")
    void should_throw_when_symbol_already_exists() {
        // given
        when(unitRepository.existsByName(UNIT_NAME)).thenReturn(false);
        when(unitRepository.existsBySymbol(UNIT_SYMBOL)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNIT_SYMBOL);
    }

    // =========================================================================
    // GROUP 4 — Cortocircuito: nombre duplicado => no se verifica símbolo ni save
    // =========================================================================

    @Test
    @DisplayName("execute never checks symbol or saves when name conflict is detected first")
    void should_not_check_symbol_or_save_when_name_is_duplicated() {
        // given
        when(unitRepository.existsByName(UNIT_NAME)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(unitRepository, never()).existsBySymbol(any());
        verify(unitRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 5 — Cortocircuito: símbolo duplicado => save nunca se llama
    // =========================================================================

    @Test
    @DisplayName("execute never calls save when symbol conflict is detected")
    void should_never_call_save_when_symbol_is_duplicated() {
        // given
        when(unitRepository.existsByName(UNIT_NAME)).thenReturn(false);
        when(unitRepository.existsBySymbol(UNIT_SYMBOL)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(unitRepository, never()).save(any());
    }
}
