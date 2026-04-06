package com.miapp.inventory_system.products.application.usecase.category;

import com.miapp.inventory_system.products.domain.model.Category;
import com.miapp.inventory_system.products.domain.repository.CategoryRepository;
import com.miapp.inventory_system.products.domain.repository.ProductRepository;
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
class DeactivateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DeactivateCategoryUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Long CATEGORY_ID = 7L;

    private Category activeCategory() {
        return Category.reconstitute(
                CATEGORY_ID, "Electrónica", true, LocalDateTime.now());
    }

    private Category inactiveCategory() {
        return Category.reconstitute(
                CATEGORY_ID, "Electrónica", false, LocalDateTime.now());
    }

    // =========================================================================
    // GROUP 1 — Happy path: categoría activa sin productos activos, se desactiva
    // =========================================================================

    @Test
    @DisplayName("execute deactivates category when it exists and has no active products")
    void should_deactivate_category_successfully() {
        // given
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(activeCategory()));
        when(productRepository.existsActiveByCategoryId(CATEGORY_ID)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(CATEGORY_ID);

        // then — el objeto pasado a save debe tener active = false
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    // =========================================================================
    // GROUP 2 — Categoría no encontrada
    // =========================================================================

    @Test
    @DisplayName("execute throws ResourceNotFoundException when category does not exist")
    void should_throw_when_category_not_found() {
        // given
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(CATEGORY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(CATEGORY_ID));

        verify(productRepository, never()).existsActiveByCategoryId(any());
        verify(categoryRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 3 — Categoría con productos activos: no se puede desactivar
    // =========================================================================

    @Test
    @DisplayName("execute throws when category has active products associated")
    void should_throw_when_category_has_active_products() {
        // given
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(activeCategory()));
        when(productRepository.existsActiveByCategoryId(CATEGORY_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(CATEGORY_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productos activos");

        verify(categoryRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 4 — Cortocircuito: si no existe, no se consultan productos ni save
    // =========================================================================

    @Test
    @DisplayName("execute never checks active products or saves when category is not found")
    void should_not_check_products_or_save_when_category_not_found() {
        // given
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> useCase.execute(CATEGORY_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        // then
        verify(productRepository, never()).existsActiveByCategoryId(any());
        verify(categoryRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 5 — Categoría ya inactiva: domain lanza excepción antes de persistir
    // =========================================================================

    @Test
    @DisplayName("execute throws when category is already inactive")
    void should_throw_when_category_is_already_inactive() {
        // given — categoría reconstituida con active=false
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(inactiveCategory()));
        when(productRepository.existsActiveByCategoryId(CATEGORY_ID)).thenReturn(false);

        // when / then — el dominio lanza la excepción en category.deactivate()
        assertThatThrownBy(() -> useCase.execute(CATEGORY_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desactivada");

        verify(categoryRepository, never()).save(any());
    }
}
