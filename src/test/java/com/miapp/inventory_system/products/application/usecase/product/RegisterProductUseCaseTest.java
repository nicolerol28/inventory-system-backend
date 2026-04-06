package com.miapp.inventory_system.products.application.usecase.product;

import com.miapp.inventory_system.products.application.command.product.RegisterProductCommand;
import com.miapp.inventory_system.products.domain.model.Product;
import com.miapp.inventory_system.products.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private RegisterProductUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers: valores base reutilizables
    // -------------------------------------------------------------------------

    private static final String PRODUCT_NAME = "Teclado Mecánico";
    private static final String DESCRIPTION  = "Teclado mecánico RGB";
    private static final String PRODUCT_SKU  = "TEC-MEC-001";
    private static final Long   UNIT_ID      = 1L;
    private static final Long   CATEGORY_ID  = 2L;
    private static final Long   SUPPLIER_ID  = 3L;

    private static final Optional<BigDecimal> PURCHASE_PRICE =
            Optional.of(new BigDecimal("50.00"));
    private static final Optional<BigDecimal> SALE_PRICE =
            Optional.of(new BigDecimal("89.99"));

    /** Construye un command válido sin imageUrl. */
    private RegisterProductCommand buildCommand() {
        return new RegisterProductCommand(
                PRODUCT_NAME, DESCRIPTION, PRODUCT_SKU,
                UNIT_ID, CATEGORY_ID, SUPPLIER_ID,
                PURCHASE_PRICE, SALE_PRICE,
                Optional.empty());
    }

    /** Construye un command válido con imageUrl. */
    private RegisterProductCommand buildCommandWithImage(String imageUrl) {
        return new RegisterProductCommand(
                PRODUCT_NAME, DESCRIPTION, PRODUCT_SKU,
                UNIT_ID, CATEGORY_ID, SUPPLIER_ID,
                PURCHASE_PRICE, SALE_PRICE,
                Optional.of(imageUrl));
    }

    // Configura el comportamiento de save para devolver el mismo objeto recibido
    private void mockSavePassThrough() {
        when(productRepository.save(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // =========================================================================
    // GROUP 1 — Happy path sin imageUrl
    // =========================================================================

    @Test
    @DisplayName("execute registers product successfully when no imageUrl is provided")
    void should_register_product_successfully_without_image_url() {
        // given
        RegisterProductCommand command = buildCommand();
        when(productRepository.existsByName(command.name())).thenReturn(false);
        when(productRepository.existsBySku(command.sku())).thenReturn(false);
        mockSavePassThrough();

        // when
        Product result = useCase.execute(command);

        // then — campos del command presentes en el producto retornado
        assertThat(result.getName()).isEqualTo(PRODUCT_NAME);
        assertThat(result.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(result.getSku()).isEqualTo(PRODUCT_SKU);
        assertThat(result.getUnitId()).isEqualTo(UNIT_ID);
        assertThat(result.getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(result.getSupplierId()).isEqualTo(SUPPLIER_ID);
        assertThat(result.getPurchasePrice()).isEqualTo(PURCHASE_PRICE);
        assertThat(result.getSalePrice()).isEqualTo(SALE_PRICE);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getImageUrl()).isNull();

        verify(productRepository).save(any(Product.class));
    }

    // =========================================================================
    // GROUP 2 — Happy path con imageUrl
    // =========================================================================

    @Test
    @DisplayName("execute registers product and sets imageUrl when provided")
    void should_register_product_with_image_url() {
        // given
        String imageUrl = "https://cdn.example.com/images/teclado-mecanico.webp";
        RegisterProductCommand command = buildCommandWithImage(imageUrl);
        when(productRepository.existsByName(command.name())).thenReturn(false);
        when(productRepository.existsBySku(command.sku())).thenReturn(false);
        mockSavePassThrough();

        // when
        Product result = useCase.execute(command);

        // then — imageUrl propagada correctamente al producto guardado
        assertThat(result.getImageUrl()).isEqualTo(imageUrl);
        assertThat(result.getName()).isEqualTo(PRODUCT_NAME);
        assertThat(result.isActive()).isTrue();

        verify(productRepository).save(any(Product.class));
    }

    // =========================================================================
    // GROUP 3 — Nombre duplicado
    // =========================================================================

    @Test
    @DisplayName("execute throws IllegalArgumentException when product name already exists")
    void should_throw_when_product_name_already_exists() {
        // given
        RegisterProductCommand command = buildCommand();
        when(productRepository.existsByName(command.name())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(PRODUCT_NAME);
    }

    // =========================================================================
    // GROUP 4 — SKU duplicado
    // =========================================================================

    @Test
    @DisplayName("execute throws IllegalArgumentException when product SKU already exists")
    void should_throw_when_product_sku_already_exists() {
        // given
        RegisterProductCommand command = buildCommand();
        when(productRepository.existsByName(command.name())).thenReturn(false);
        when(productRepository.existsBySku(command.sku())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU");
    }

    // =========================================================================
    // GROUP 5 — Cortocircuito: si el nombre existe, no se consulta SKU ni save
    // =========================================================================

    @Test
    @DisplayName("execute never checks SKU or calls save when name is already taken")
    void should_not_check_sku_or_save_when_name_already_exists() {
        // given
        RegisterProductCommand command = buildCommand();
        when(productRepository.existsByName(command.name())).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        // then — las llamadas subsiguientes nunca deben ocurrir
        verify(productRepository, never()).existsBySku(any());
        verify(productRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 6 — Cortocircuito: si el SKU existe, save nunca es llamado
    // =========================================================================

    @Test
    @DisplayName("execute never calls save when SKU is already taken")
    void should_not_save_when_sku_already_exists() {
        // given
        RegisterProductCommand command = buildCommand();
        when(productRepository.existsByName(command.name())).thenReturn(false);
        when(productRepository.existsBySku(command.sku())).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        // then — nunca debe persistirse nada
        verify(productRepository, never()).save(any());
    }
}