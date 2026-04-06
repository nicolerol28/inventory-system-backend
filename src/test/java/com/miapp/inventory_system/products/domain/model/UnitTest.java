package com.miapp.inventory_system.products.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnitTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final String VALID_NAME   = "Kilogramo";
    private static final String VALID_SYMBOL = "kg";

    // =========================================================================
    // GROUP 1 — Unit.create(...): happy path
    // =========================================================================

    @Test
    @DisplayName("create sets name, symbol, active=true, createdAt not null and id=null")
    void should_create_unit_with_correct_initial_state() {
        // given / when
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Unit unit = Unit.create(VALID_NAME, VALID_SYMBOL);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // then
        assertThat(unit.getName()).isEqualTo(VALID_NAME);
        assertThat(unit.getSymbol()).isEqualTo(VALID_SYMBOL);
        assertThat(unit.isActive()).isTrue();
        assertThat(unit.getId()).isNull();
        assertThat(unit.getCreatedAt())
                .isNotNull()
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    // =========================================================================
    // GROUP 2 — Unit.create(...): campos inválidos
    // =========================================================================

    static Stream<Arguments> invalidFieldsForCreate() {
        return Stream.of(
                Arguments.of(null,       VALID_SYMBOL, "nombre de la unidad no puede estar vacío"),
                Arguments.of("   ",      VALID_SYMBOL, "nombre de la unidad no puede estar vacío"),
                Arguments.of(VALID_NAME, null,          "símbolo de la unidad no puede estar vacío"),
                Arguments.of(VALID_NAME, "   ",         "símbolo de la unidad no puede estar vacío")
        );
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("invalidFieldsForCreate")
    @DisplayName("create throws IllegalArgumentException for each invalid field")
    void should_throw_when_fields_are_invalid_on_create(
            String name, String symbol, String expectedMessage) {

        assertThatThrownBy(() -> Unit.create(name, symbol))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    // =========================================================================
    // GROUP 3 — Unit.reconstitute(...): mapeo exacto
    // =========================================================================

    @Test
    @DisplayName("reconstitute maps all fields exactly, including active=false")
    void should_reconstitute_unit_with_all_fields_mapped_exactly() {
        // given
        Long          id        = 15L;
        String        name      = "Litro";
        String        symbol    = "L";
        boolean       active    = false;
        LocalDateTime createdAt = LocalDateTime.of(2024, 5, 20, 14, 0);

        // when
        Unit unit = Unit.reconstitute(id, name, symbol, active, createdAt);

        // then
        assertThat(unit.getId()).isEqualTo(id);
        assertThat(unit.getName()).isEqualTo(name);
        assertThat(unit.getSymbol()).isEqualTo(symbol);
        assertThat(unit.isActive()).isFalse();
        assertThat(unit.getCreatedAt()).isEqualTo(createdAt);
    }

    // =========================================================================
    // GROUP 4 — Unit.update(...): happy path
    // =========================================================================

    @Test
    @DisplayName("update changes name and symbol of the unit")
    void should_update_unit_name_and_symbol_successfully() {
        // given
        Unit unit = Unit.create(VALID_NAME, VALID_SYMBOL);

        // when
        unit.update("Gramo", "g");

        // then
        assertThat(unit.getName()).isEqualTo("Gramo");
        assertThat(unit.getSymbol()).isEqualTo("g");
    }

    // =========================================================================
    // GROUP 5 — Unit.update(...): campos inválidos
    // =========================================================================

    static Stream<Arguments> invalidFieldsForUpdate() {
        return Stream.of(
                Arguments.of(null,       VALID_SYMBOL, "nombre de la unidad no puede estar vacío"),
                Arguments.of("   ",      VALID_SYMBOL, "nombre de la unidad no puede estar vacío"),
                Arguments.of(VALID_NAME, null,          "símbolo de la unidad no puede estar vacío"),
                Arguments.of(VALID_NAME, "   ",         "símbolo de la unidad no puede estar vacío")
        );
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("invalidFieldsForUpdate")
    @DisplayName("update throws IllegalArgumentException for each invalid field")
    void should_throw_when_fields_are_invalid_on_update(
            String name, String symbol, String expectedMessage) {

        Unit unit = Unit.create(VALID_NAME, VALID_SYMBOL);

        assertThatThrownBy(() -> unit.update(name, symbol))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    // =========================================================================
    // GROUP 6 — Unit.deactivate(): happy path y estado ya inactivo
    // =========================================================================

    @Test
    @DisplayName("deactivate sets active to false on an active unit")
    void should_deactivate_active_unit() {
        // given
        Unit unit = Unit.create(VALID_NAME, VALID_SYMBOL);
        assertThat(unit.isActive()).isTrue();

        // when
        unit.deactivate();

        // then
        assertThat(unit.isActive()).isFalse();
    }

    @Test
    @DisplayName("deactivate throws when unit is already inactive")
    void should_throw_when_deactivating_already_inactive_unit() {
        // given
        Unit unit = Unit.reconstitute(
                1L, VALID_NAME, VALID_SYMBOL, false, LocalDateTime.now());

        // when / then
        assertThatThrownBy(unit::deactivate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desactivada");
    }
}
