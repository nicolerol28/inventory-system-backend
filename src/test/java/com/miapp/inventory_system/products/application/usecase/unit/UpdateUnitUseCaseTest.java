package com.miapp.inventory_system.products.application.usecase.unit;

import com.miapp.inventory_system.products.application.command.unit.UpdateUnitCommand;
import com.miapp.inventory_system.products.domain.model.Unit;
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
class UpdateUnitUseCaseTest {

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private UpdateUnitUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Long   UNIT_ID     = 3L;
    private static final String UNIT_NAME   = "Gramo";
    private static final String UNIT_SYMBOL = "g";

    private UpdateUnitCommand buildCommand() {
        return new UpdateUnitCommand(UNIT_ID, UNIT_NAME, UNIT_SYMBOL);
    }

    private Unit existingUnit() {
        return Unit.reconstitute(UNIT_ID, "Kilogramo", "kg", true, LocalDateTime.now());
    }

    private void mockSavePassThrough() {
        when(unitRepository.save(any(Unit.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // =========================================================================
    // GROUP 1 — Happy path: nombre y símbolo actualizados, save llamado una vez
    // =========================================================================

    @Test
    @DisplayName("execute updates unit name and symbol and calls save once")
    void should_update_unit_successfully() {
        // given
        UpdateUnitCommand command = buildCommand();
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(existingUnit()));
        when(unitRepository.existsByNameAndIdNot(UNIT_NAME, UNIT_ID)).thenReturn(false);
        when(unitRepository.existsBySymbolAndIdNot(UNIT_SYMBOL, UNIT_ID)).thenReturn(false);
        mockSavePassThrough();

        ArgumentCaptor<Unit> captor = ArgumentCaptor.forClass(Unit.class);

        // when
        Unit result = useCase.execute(command);

        // then — campos actualizados
        verify(unitRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(UNIT_NAME);
        assertThat(captor.getValue().getSymbol()).isEqualTo(UNIT_SYMBOL);
        assertThat(result.getName()).isEqualTo(UNIT_NAME);
        assertThat(result.getSymbol()).isEqualTo(UNIT_SYMBOL);
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
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(UNIT_ID));

        verify(unitRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 3 — Nombre duplicado en otra unidad
    // =========================================================================

    @Test
    @DisplayName("execute throws when another unit already has the same name")
    void should_throw_when_name_belongs_to_another_unit() {
        // given
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(existingUnit()));
        when(unitRepository.existsByNameAndIdNot(UNIT_NAME, UNIT_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNIT_NAME);

        verify(unitRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 4 — Símbolo duplicado en otra unidad
    // =========================================================================

    @Test
    @DisplayName("execute throws when another unit already has the same symbol")
    void should_throw_when_symbol_belongs_to_another_unit() {
        // given
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(existingUnit()));
        when(unitRepository.existsByNameAndIdNot(UNIT_NAME, UNIT_ID)).thenReturn(false);
        when(unitRepository.existsBySymbolAndIdNot(UNIT_SYMBOL, UNIT_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNIT_SYMBOL);

        verify(unitRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 5 — Cortocircuito: nombre duplicado => no se verifica símbolo ni save
    // =========================================================================

    @Test
    @DisplayName("execute never checks symbol or saves when name conflict is detected first")
    void should_not_check_symbol_or_save_when_name_conflict_detected() {
        // given
        when(unitRepository.findById(UNIT_ID)).thenReturn(Optional.of(existingUnit()));
        when(unitRepository.existsByNameAndIdNot(UNIT_NAME, UNIT_ID)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(unitRepository, never()).existsBySymbolAndIdNot(any(), any());
        verify(unitRepository, never()).save(any());
    }
}
