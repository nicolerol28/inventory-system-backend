package com.miapp.inventory_system.products.application.usecase.category;

import com.miapp.inventory_system.products.application.command.category.UpdateCategoryCommand;
import com.miapp.inventory_system.products.domain.model.Category;
import com.miapp.inventory_system.products.domain.repository.CategoryRepository;
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
class UpdateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private UpdateCategoryUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Long   CATEGORY_ID   = 5L;
    private static final String CATEGORY_NAME = "Informática";

    private UpdateCategoryCommand buildCommand() {
        return new UpdateCategoryCommand(CATEGORY_ID, CATEGORY_NAME);
    }

    private Category existingCategory() {
        return Category.reconstitute(
                CATEGORY_ID, "Nombre anterior", true, LocalDateTime.now());
    }

    private void mockSavePassThrough() {
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // =========================================================================
    // GROUP 1 — Happy path: nombre actualizado, save llamado una vez
    // =========================================================================

    @Test
    @DisplayName("execute updates category name and calls save once")
    void should_update_category_name_successfully() {
        // given
        UpdateCategoryCommand command = buildCommand();
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existingCategory()));
        when(categoryRepository.existsByNameAndIdNot(CATEGORY_NAME, CATEGORY_ID)).thenReturn(false);
        mockSavePassThrough();

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);

        // when
        Category result = useCase.execute(command);

        // then — nombre actualizado
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(CATEGORY_NAME);
        assertThat(result.getName()).isEqualTo(CATEGORY_NAME);
    }

    // =========================================================================
    // GROUP 2 — Categoría no encontrada
    // =========================================================================

    @Test
    @DisplayName("execute throws ResourceNotFoundException when category does not exist")
    void should_throw_when_category_not_found() {
        // given
        UpdateCategoryCommand command = buildCommand();
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(CATEGORY_ID));

        verify(categoryRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 3 — Nombre duplicado en otra categoría
    // =========================================================================

    @Test
    @DisplayName("execute throws when another category already has the same name")
    void should_throw_when_name_belongs_to_another_category() {
        // given
        UpdateCategoryCommand command = buildCommand();
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existingCategory()));
        when(categoryRepository.existsByNameAndIdNot(CATEGORY_NAME, CATEGORY_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(CATEGORY_NAME);

        verify(categoryRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 4 — Cortocircuito: nombre duplicado => save nunca se llama
    // =========================================================================

    @Test
    @DisplayName("execute never calls save when name conflict is detected")
    void should_never_call_save_when_name_conflict_detected() {
        // given
        UpdateCategoryCommand command = buildCommand();
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existingCategory()));
        when(categoryRepository.existsByNameAndIdNot(CATEGORY_NAME, CATEGORY_ID)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(categoryRepository, never()).save(any());
    }
}
