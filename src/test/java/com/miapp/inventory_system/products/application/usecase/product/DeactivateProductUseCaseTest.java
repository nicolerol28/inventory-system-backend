package com.miapp.inventory_system.products.application.usecase.product;

import com.miapp.inventory_system.products.domain.ProductStockChecker;
import com.miapp.inventory_system.products.domain.model.Product;
import com.miapp.inventory_system.products.domain.repository.ProductRepository;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeactivateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductStockChecker productStockChecker;

    @InjectMocks
    private DeactivateProductUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Long PRODUCT_ID = 10L;

    private Product activeProduct() {
        return Product.reconstitute(
                PRODUCT_ID, "Teclado Mecánico", "Teclado RGB", "TEC-MEC-001",
                1L, 2L, 3L,
                Optional.of(new BigDecimal("50.00")),
                Optional.of(new BigDecimal("89.99")),
                true, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    private Product inactiveProduct() {
        return Product.reconstitute(
                PRODUCT_ID, "Teclado Mecánico", "Teclado RGB", "TEC-MEC-001",
                1L, 2L, 3L,
                Optional.of(new BigDecimal("50.00")),
                Optional.of(new BigDecimal("89.99")),
                false, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    // =========================================================================
    // GROUP 1 — Happy path: producto activo sin stock, se desactiva correctamente
    // =========================================================================

    @Test
    @DisplayName("execute deactivates product when it exists and has no active stock")
    void should_deactivate_product_successfully() {
        // given
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(activeProduct()));
        when(productStockChecker.hasActiveStockByProductId(PRODUCT_ID)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(PRODUCT_ID);

        // then — el objeto pasado a save debe tener active = false
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    // =========================================================================
    // GROUP 2 — Producto no encontrado
    // =========================================================================

    @Test
    @DisplayName("execute throws ResourceNotFoundException when product does not exist")
    void should_throw_when_product_not_found() {
        // given
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(PRODUCT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(PRODUCT_ID));

        verify(productStockChecker, never()).hasActiveStockByProductId(any());
        verify(productRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 3 — Producto con stock activo: no se puede desactivar
    // =========================================================================

    @Test
    @DisplayName("execute throws when product has active stock associated")
    void should_throw_when_product_has_active_stock() {
        // given
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(activeProduct()));
        when(productStockChecker.hasActiveStockByProductId(PRODUCT_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(PRODUCT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock");

        verify(productRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 4 — Producto ya inactivo: domain lanza excepción antes de persistir
    // =========================================================================

    @Test
    @DisplayName("execute throws when product is already inactive")
    void should_throw_when_product_is_already_inactive() {
        // given — producto reconstituido con active=false
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(inactiveProduct()));
        when(productStockChecker.hasActiveStockByProductId(PRODUCT_ID)).thenReturn(false);

        // when / then — el dominio lanza la excepción en product.deactivate()
        assertThatThrownBy(() -> useCase.execute(PRODUCT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desactivado");

        verify(productRepository, never()).save(any());
    }
}