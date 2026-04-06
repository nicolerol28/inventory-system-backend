package com.miapp.inventory_system.warehouse.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarehouseTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final String VALID_NAME     = "Almacén Central";
    private static final String VALID_LOCATION = "Av. Siempre Viva 123";

    // =========================================================================
    // GROUP 1 — Warehouse.create(...): happy path
    // =========================================================================

    @Test
    @DisplayName("create sets name, location, active=true, createdAt not null and id=null")
    void should_create_warehouse_with_correct_initial_state() {
        // given / when
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Warehouse warehouse = Warehouse.create(VALID_NAME, VALID_LOCATION);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // then
        assertThat(warehouse.getName()).isEqualTo(VALID_NAME);
        assertThat(warehouse.getLocation()).isEqualTo(VALID_LOCATION);
        assertThat(warehouse.isActive()).isTrue();
        assertThat(warehouse.getId()).isNull();
        assertThat(warehouse.getCreatedAt())
                .isNotNull()
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("create accepts null location (location is optional)")
    void should_create_warehouse_when_location_is_null() {
        // given / when
        Warehouse warehouse = Warehouse.create(VALID_NAME, null);

        // then
        assertThat(warehouse.getLocation()).isNull();
        assertThat(warehouse.isActive()).isTrue();
    }

    // =========================================================================
    // GROUP 2 — Warehouse.create(...): validaciones de nombre
    // =========================================================================

    @Test
    @DisplayName("create throws when name is null")
    void should_throw_when_name_is_null_on_create() {
        assertThatThrownBy(() -> Warehouse.create(null, VALID_LOCATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre del almacen es obligatorio");
    }

    @Test
    @DisplayName("create throws when name is blank")
    void should_throw_when_name_is_blank_on_create() {
        assertThatThrownBy(() -> Warehouse.create("   ", VALID_LOCATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre del almacen es obligatorio");
    }

    // =========================================================================
    // GROUP 3 — Warehouse.reconstitute(...): mapeo exacto
    // =========================================================================

    @Test
    @DisplayName("reconstitute maps all fields exactly, including active=false")
    void should_reconstitute_warehouse_with_all_fields_mapped_exactly() {
        // given
        Long          id        = 7L;
        String        name      = "Depósito Norte";
        String        location  = "Calle Falsa 456";
        boolean       active    = false;
        LocalDateTime createdAt = LocalDateTime.of(2024, 8, 1, 8, 0);

        // when
        Warehouse warehouse = Warehouse.reconstitute(id, name, location, active, createdAt);

        // then — cada campo debe coincidir exactamente con el parámetro pasado
        assertThat(warehouse.getId()).isEqualTo(id);
        assertThat(warehouse.getName()).isEqualTo(name);
        assertThat(warehouse.getLocation()).isEqualTo(location);
        assertThat(warehouse.isActive()).isFalse();
        assertThat(warehouse.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("reconstitute accepts null location without throwing")
    void should_reconstitute_warehouse_with_null_location() {
        // given / when
        Warehouse warehouse = Warehouse.reconstitute(
                1L, VALID_NAME, null, true, LocalDateTime.now());

        // then
        assertThat(warehouse.getLocation()).isNull();
    }

    // =========================================================================
    // GROUP 4 — Warehouse.update(...): happy path y validaciones
    // =========================================================================

    @Test
    @DisplayName("update changes name and location of the warehouse")
    void should_update_warehouse_name_and_location_successfully() {
        // given
        Warehouse warehouse = Warehouse.create(VALID_NAME, VALID_LOCATION);

        // when
        warehouse.update("Almacén Sur", "Ruta 9 km 50");

        // then
        assertThat(warehouse.getName()).isEqualTo("Almacén Sur");
        assertThat(warehouse.getLocation()).isEqualTo("Ruta 9 km 50");
    }

    @Test
    @DisplayName("update accepts null location")
    void should_update_warehouse_with_null_location() {
        // given
        Warehouse warehouse = Warehouse.create(VALID_NAME, VALID_LOCATION);

        // when
        warehouse.update(VALID_NAME, null);

        // then
        assertThat(warehouse.getLocation()).isNull();
    }

    @Test
    @DisplayName("update throws when new name is null")
    void should_throw_when_name_is_null_on_update() {
        Warehouse warehouse = Warehouse.create(VALID_NAME, VALID_LOCATION);

        assertThatThrownBy(() -> warehouse.update(null, VALID_LOCATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre del almacen es obligatorio");
    }

    @Test
    @DisplayName("update throws when new name is blank")
    void should_throw_when_name_is_blank_on_update() {
        Warehouse warehouse = Warehouse.create(VALID_NAME, VALID_LOCATION);

        assertThatThrownBy(() -> warehouse.update("  ", VALID_LOCATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre del almacen es obligatorio");
    }

    // =========================================================================
    // GROUP 5 — Warehouse.deactivate(): happy path y estado ya inactivo
    // =========================================================================

    @Test
    @DisplayName("deactivate sets active to false on an active warehouse")
    void should_deactivate_active_warehouse() {
        // given
        Warehouse warehouse = Warehouse.create(VALID_NAME, VALID_LOCATION);
        assertThat(warehouse.isActive()).isTrue();

        // when
        warehouse.deactivate();

        // then
        assertThat(warehouse.isActive()).isFalse();
    }

    @Test
    @DisplayName("deactivate throws when warehouse is already inactive")
    void should_throw_when_deactivating_already_inactive_warehouse() {
        // given — almacén reconstituido con active=false
        Warehouse warehouse = Warehouse.reconstitute(
                1L, VALID_NAME, VALID_LOCATION, false, LocalDateTime.now());

        // when / then
        assertThatThrownBy(warehouse::deactivate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("almacen ya está desactivado");
    }
}
