package com.miapp.inventory_system.products.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final String VALID_NAME = "Electrónica";

    // =========================================================================
    // GROUP 1 — Category.create(...): happy path
    // =========================================================================

    @Test
    @DisplayName("create sets name, active=true, createdAt not null and id=null")
    void should_create_category_with_correct_initial_state() {
        // given / when
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Category category = Category.create(VALID_NAME);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // then
        assertThat(category.getName()).isEqualTo(VALID_NAME);
        assertThat(category.isActive()).isTrue();
        assertThat(category.getId()).isNull();
        assertThat(category.getCreatedAt())
                .isNotNull()
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    // =========================================================================
    // GROUP 2 — Category.create(...): validaciones de nombre
    // =========================================================================

    @Test
    @DisplayName("create throws when name is null")
    void should_throw_when_name_is_null_on_create() {
        assertThatThrownBy(() -> Category.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre de la categoría no puede ser nulo o vacío");
    }

    @Test
    @DisplayName("create throws when name is blank")
    void should_throw_when_name_is_blank_on_create() {
        assertThatThrownBy(() -> Category.create("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre de la categoría no puede ser nulo o vacío");
    }

    // =========================================================================
    // GROUP 3 — Category.reconstitute(...): mapeo exacto
    // =========================================================================

    @Test
    @DisplayName("reconstitute maps all fields exactly, including active=false")
    void should_reconstitute_category_with_all_fields_mapped_exactly() {
        // given
        Long          id        = 42L;
        String        name      = "Herramientas";
        boolean       active    = false;
        LocalDateTime createdAt = LocalDateTime.of(2024, 3, 10, 9, 30);

        // when
        Category category = Category.reconstitute(id, name, active, createdAt);

        // then — cada campo debe coincidir exactamente con el parámetro pasado
        assertThat(category.getId()).isEqualTo(id);
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.isActive()).isFalse();
        assertThat(category.getCreatedAt()).isEqualTo(createdAt);
    }

    // =========================================================================
    // GROUP 4 — Category.update(...): happy path y validaciones
    // =========================================================================

    @Test
    @DisplayName("update changes the name of the category")
    void should_update_category_name_successfully() {
        // given
        Category category = Category.create(VALID_NAME);
        String newName = "Informática";

        // when
        category.update(newName);

        // then
        assertThat(category.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("update throws when new name is null")
    void should_throw_when_name_is_null_on_update() {
        // given
        Category category = Category.create(VALID_NAME);

        // when / then
        assertThatThrownBy(() -> category.update(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre de la categoría no puede ser nulo o vacío");
    }

    @Test
    @DisplayName("update throws when new name is blank")
    void should_throw_when_name_is_blank_on_update() {
        // given
        Category category = Category.create(VALID_NAME);

        // when / then
        assertThatThrownBy(() -> category.update("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre de la categoría no puede ser nulo o vacío");
    }

    // =========================================================================
    // GROUP 5 — Category.deactivate(): happy path y estado ya inactivo
    // =========================================================================

    @Test
    @DisplayName("deactivate sets active to false on an active category")
    void should_deactivate_active_category() {
        // given
        Category category = Category.create(VALID_NAME);
        assertThat(category.isActive()).isTrue();

        // when
        category.deactivate();

        // then
        assertThat(category.isActive()).isFalse();
    }

    @Test
    @DisplayName("deactivate throws when category is already inactive")
    void should_throw_when_deactivating_already_inactive_category() {
        // given — categoría reconstituida con active=false
        Category category = Category.reconstitute(
                1L, VALID_NAME, false, LocalDateTime.now());

        // when / then
        assertThatThrownBy(category::deactivate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desactivada");
    }
}
