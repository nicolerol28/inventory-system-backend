package com.miapp.inventory_system.suppliers.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierTest {

    private static final String VALID_NAME    = "Distribuidora S.A.";
    private static final String VALID_CONTACT = "Ana Torres";
    private static final String VALID_PHONE   = "987654321";

    // -------------------------------------------------------------------------
    // GROUP 1 — create: happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create sets active=true, non-null timestamps, and assigns all fields correctly")
    void should_create_supplier_with_correct_initial_state() {
        // given / when
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Supplier supplier = Supplier.create(VALID_NAME, VALID_CONTACT, VALID_PHONE);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // then
        assertThat(supplier.getId()).isNull();
        assertThat(supplier.getName()).isEqualTo(VALID_NAME);
        assertThat(supplier.getContact()).isEqualTo(VALID_CONTACT);
        assertThat(supplier.getPhone()).isEqualTo(VALID_PHONE);
        assertThat(supplier.isActive()).isTrue();
        assertThat(supplier.getCreatedAt()).isNotNull().isBetween(before, after);
        assertThat(supplier.getUpdatedAt()).isNotNull().isBetween(before, after);
    }

    @Test
    @DisplayName("create allows null contact and null phone (optional fields)")
    void should_create_supplier_with_null_optional_fields() {
        // given / when
        Supplier supplier = Supplier.create(VALID_NAME, null, null);

        // then
        assertThat(supplier.getName()).isEqualTo(VALID_NAME);
        assertThat(supplier.getContact()).isNull();
        assertThat(supplier.getPhone()).isNull();
        assertThat(supplier.isActive()).isTrue();
    }

    // -------------------------------------------------------------------------
    // GROUP 2 — create: invalid name
    // -------------------------------------------------------------------------

    static Stream<Arguments> invalidNamesForCreate() {
        return Stream.of(
                Arguments.of((String) null, "El nombre del proveedor es obligatorio"),
                Arguments.of("   ",         "El nombre del proveedor es obligatorio")
        );
    }

    @ParameterizedTest(name = "[{index}] name=''{0}'' → {1}")
    @MethodSource("invalidNamesForCreate")
    @DisplayName("create throws IllegalArgumentException for each invalid name")
    void should_throw_when_name_is_invalid_on_create(String name, String expectedMessage) {
        assertThatThrownBy(() -> Supplier.create(name, VALID_CONTACT, VALID_PHONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    // -------------------------------------------------------------------------
    // GROUP 3 — reconstitute
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("reconstitute preserves all fields exactly without validation")
    void should_reconstitute_all_fields_without_validation() {
        // given
        LocalDateTime createdAt  = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        LocalDateTime updatedAt  = LocalDateTime.of(2024, 6, 20, 12, 30, 0);

        // when
        Supplier supplier = Supplier.reconstitute(
                42L, VALID_NAME, VALID_CONTACT, VALID_PHONE,
                false, createdAt, updatedAt);

        // then
        assertThat(supplier.getId()).isEqualTo(42L);
        assertThat(supplier.getName()).isEqualTo(VALID_NAME);
        assertThat(supplier.getContact()).isEqualTo(VALID_CONTACT);
        assertThat(supplier.getPhone()).isEqualTo(VALID_PHONE);
        assertThat(supplier.isActive()).isFalse();
        assertThat(supplier.getCreatedAt()).isEqualTo(createdAt);
        assertThat(supplier.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("reconstitute does not validate name — accepts blank without throwing")
    void should_reconstitute_with_blank_name_without_throwing() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when / then — reconstitute no lanza excepción aunque el nombre sea inválido
        Supplier supplier = Supplier.reconstitute(1L, "   ", null, null, true, now, now);
        assertThat(supplier.getName()).isEqualTo("   ");
    }

    // -------------------------------------------------------------------------
    // GROUP 4 — update: happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("update replaces name, contact and phone, and refreshes updatedAt")
    void should_update_fields_and_refresh_updated_at() throws InterruptedException {
        // given
        Supplier supplier = Supplier.create(VALID_NAME, VALID_CONTACT, VALID_PHONE);
        LocalDateTime updatedAtBeforeUpdate = supplier.getUpdatedAt();

        // small pause to ensure timestamp difference is detectable
        Thread.sleep(5);

        // when
        supplier.update("Nuevo Proveedor", "Luis Gómez", "111222333");

        // then
        assertThat(supplier.getName()).isEqualTo("Nuevo Proveedor");
        assertThat(supplier.getContact()).isEqualTo("Luis Gómez");
        assertThat(supplier.getPhone()).isEqualTo("111222333");
        assertThat(supplier.getUpdatedAt()).isAfterOrEqualTo(updatedAtBeforeUpdate);
    }

    // -------------------------------------------------------------------------
    // GROUP 5 — update: invalid name
    // -------------------------------------------------------------------------

    static Stream<Arguments> invalidNamesForUpdate() {
        return Stream.of(
                Arguments.of((String) null, "El nombre del proveedor es obligatorio"),
                Arguments.of("   ",         "El nombre del proveedor es obligatorio")
        );
    }

    @ParameterizedTest(name = "[{index}] name=''{0}'' → {1}")
    @MethodSource("invalidNamesForUpdate")
    @DisplayName("update throws IllegalArgumentException for each invalid name")
    void should_throw_when_name_is_invalid_on_update(String invalidName, String expectedMessage) {
        // given
        Supplier supplier = Supplier.create(VALID_NAME, VALID_CONTACT, VALID_PHONE);

        // when / then
        assertThatThrownBy(() -> supplier.update(invalidName, VALID_CONTACT, VALID_PHONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    // -------------------------------------------------------------------------
    // GROUP 6 — deactivate: happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deactivate sets active to false on an active supplier")
    void should_deactivate_active_supplier() {
        // given
        Supplier supplier = Supplier.create(VALID_NAME, VALID_CONTACT, VALID_PHONE);
        assertThat(supplier.isActive()).isTrue();

        // when
        supplier.deactivate();

        // then
        assertThat(supplier.isActive()).isFalse();
    }

    // -------------------------------------------------------------------------
    // GROUP 7 — deactivate: already inactive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deactivate throws when supplier is already inactive")
    void should_throw_when_deactivating_already_inactive_supplier() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Supplier supplier = Supplier.reconstitute(1L, VALID_NAME, VALID_CONTACT, VALID_PHONE,
                false, now, now);

        // when / then
        assertThatThrownBy(supplier::deactivate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El proveedor ya está desactivado");
    }
}
