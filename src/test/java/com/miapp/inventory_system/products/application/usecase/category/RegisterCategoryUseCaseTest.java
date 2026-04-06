package com.miapp.inventory_system.products.application.usecase.category;

import com.miapp.inventory_system.products.application.command.category.RegisterCategoryCommand;
import com.miapp.inventory_system.products.domain.model.Category;
import com.miapp.inventory_system.products.domain.repository.CategoryRepository;
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
class RegisterCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private RegisterCategoryUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final String CATEGORY_NAME = "Electrónica";

    private RegisterCategoryCommand buildCommand() {
        return new RegisterCategoryCommand(CATEGORY_NAME);
    }

    // =========================================================================
    // GROUP 1 — Happy path: categoría guardada correctamente
    // =========================================================================

    @Test
    @DisplayName("execute registers category successfully when name is not duplicated")
    void should_register_category_successfully() {
        // given
        when(categoryRepository.existsByName(CATEGORY_NAME)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            return Category.reconstitute(1L, c.getName(), c.isActive(), c.getCreatedAt());
        });

        // when
        Category result = useCase.execute(buildCommand());

        // then
        assertThat(result.getName()).isEqualTo(CATEGORY_NAME);
        assertThat(result.isActive()).isTrue();
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    // =========================================================================
    // GROUP 2 — Nombre duplicado: lanza excepción
    // =========================================================================

    @Test
    @DisplayName("execute throws when a category with the same name already exists")
    void should_throw_when_name_already_exists() {
        // given
        when(categoryRepository.existsByName(CATEGORY_NAME)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe una categoría con el mismo nombre");
    }

    // =========================================================================
    // GROUP 3 — Cortocircuito: nombre duplicado => save nunca se llama
    // =========================================================================

    @Test
    @DisplayName("execute never calls save when name conflict is detected")
    void should_never_call_save_when_name_is_duplicated() {
        // given
        when(categoryRepository.existsByName(CATEGORY_NAME)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(categoryRepository, never()).save(any());
    }
}
