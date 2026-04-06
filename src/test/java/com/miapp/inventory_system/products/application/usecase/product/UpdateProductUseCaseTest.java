package com.miapp.inventory_system.products.application.usecase.product;

import com.miapp.inventory_system.products.application.command.product.UpdateProductCommand;
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
class UpdateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UpdateProductUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Long   PRODUCT_ID   = 10L;
    private static final String PRODUCT_NAME = "Monitor UltraWide";
    private static final String DESCRIPTION  = "Monitor curvo 34 pulgadas";
    private static final String PRODUCT_SKU  = "MON-UW-34";
    private static final Long   UNIT_ID      = 1L;
    private static final Long   CATEGORY_ID  = 2L;
    private static final Long   SUPPLIER_ID  = 3L;

    private static final Optional<BigDecimal> PURCHASE_PRICE =
            Optional.of(new BigDecimal("300.00"));
    private static final Optional<BigDecimal> SALE_PRICE =
            Optional.of(new BigDecimal("450.00"));

    /** Producto existente reconstituido desde persistencia. */
    private Product existingProduct() {
        return Product.reconstitute(
                PRODUCT_ID, "Nombre anterior", "Desc anterior", "SKU-ANTERIOR",
                UNIT_ID, CATEGORY_ID, SUPPLIER_ID,
                Optional.of(new BigDecimal("200.00")),
                Optional.of(new BigDecimal("350.00")),
                true, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    /** Command válido sin imageUrl. */
    private UpdateProductCommand buildCommand() {
        return new UpdateProductCommand(
                PRODUCT_ID, PRODUCT_NAME, DESCRIPTION, PRODUCT_SKU,
                UNIT_ID, CATEGORY_ID, SUPPLIER_ID,
                PURCHASE_PRICE, SALE_PRICE,
                Optional.empty());
    }

    /** Command válido con imageUrl. */
    private UpdateProductCommand buildCommandWithImage(String imageUrl) {
        return new UpdateProductCommand(
                PRODUCT_ID, PRODUCT_NAME, DESCRIPTION, PRODUCT_SKU,
                UNIT_ID, CATEGORY_ID, SUPPLIER_ID,
                PURCHASE_PRICE, SALE_PRICE,
                Optional.of(imageUrl));
    }

    private void mockSavePassThrough() {
        when(productRepository.save(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // =========================================================================
    // GROUP 1 — Happy path sin imageUrl
    // =========================================================================

    @Test
    @DisplayName("execute updates product fields correctly when no imageUrl is provided")
    void should_update_product_successfully_without_image_url() {
        // given
        UpdateProductCommand command = buildCommand();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct()));
        when(productRepository.existsByNameAndIdNot(command.name(), PRODUCT_ID)).thenReturn(false);
        when(productRepository.existsBySkuAndIdNot(command.sku(), PRODUCT_ID)).thenReturn(false);
        mockSavePassThrough();

        // when
        Product result = useCase.execute(command);

        // then — campos actualizados
        assertThat(result.getName()).isEqualTo(PRODUCT_NAME);
        assertThat(result.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(result.getSku()).isEqualTo(PRODUCT_SKU);
        assertThat(result.getUnitId()).isEqualTo(UNIT_ID);
        assertThat(result.getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(result.getSupplierId()).isEqualTo(SUPPLIER_ID);
        assertThat(result.getPurchasePrice()).isEqualTo(PURCHASE_PRICE);
        assertThat(result.getSalePrice()).isEqualTo(SALE_PRICE);

        // then — imageUrl no debe cambiar (era null, sigue null)
        assertThat(result.getImageUrl()).isNull();

        verify(productRepository).save(any(Product.class));
    }

    // =========================================================================
    // GROUP 2 — Happy path con imageUrl
    // =========================================================================

    @Test
    @DisplayName("execute updates imageUrl when provided in command")
    void should_update_product_and_set_image_url() {
        // given
        String imageUrl = "https://cdn.example.com/monitor-uw.webp";
        UpdateProductCommand command = buildCommandWithImage(imageUrl);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct()));
        when(productRepository.existsByNameAndIdNot(command.name(), PRODUCT_ID)).thenReturn(false);
        when(productRepository.existsBySkuAndIdNot(command.sku(), PRODUCT_ID)).thenReturn(false);
        mockSavePassThrough();

        // when
        Product result = useCase.execute(command);

        // then
        assertThat(result.getImageUrl()).isEqualTo(imageUrl);
        assertThat(result.getName()).isEqualTo(PRODUCT_NAME);
    }

    // =========================================================================
    // GROUP 3 — Happy path con imageUrl preexistente: no se sobreescribe si
    //           el command no trae una nueva
    // =========================================================================

    @Test
    @DisplayName("execute preserves existing imageUrl when command provides none")
    void should_preserve_existing_image_url_when_command_has_no_image() {
        // given — producto que ya tiene imagen
        String existingImageUrl = "https://cdn.example.com/old-image.jpg";
        Product productWithImage = Product.reconstitute(
                PRODUCT_ID, "Nombre anterior", "Desc anterior", "SKU-ANTERIOR",
                UNIT_ID, CATEGORY_ID, SUPPLIER_ID,
                Optional.of(new BigDecimal("200.00")),
                Optional.of(new BigDecimal("350.00")),
                true, LocalDateTime.now(), LocalDateTime.now(), existingImageUrl);

        UpdateProductCommand command = buildCommand(); // imageUrl = Optional.empty()
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(productWithImage));
        when(productRepository.existsByNameAndIdNot(command.name(), PRODUCT_ID)).thenReturn(false);
        when(productRepository.existsBySkuAndIdNot(command.sku(), PRODUCT_ID)).thenReturn(false);
        mockSavePassThrough();

        // when
        Product result = useCase.execute(command);

        // then — la imagen original no debe haber cambiado
        assertThat(result.getImageUrl()).isEqualTo(existingImageUrl);
    }

    // =========================================================================
    // GROUP 4 — Producto no encontrado
    // =========================================================================

    @Test
    @DisplayName("execute throws ResourceNotFoundException when product does not exist")
    void should_throw_when_product_not_found() {
        // given
        UpdateProductCommand command = buildCommand();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(PRODUCT_ID));

        verify(productRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 5 — Nombre duplicado en otro producto
    // =========================================================================

    @Test
    @DisplayName("execute throws when another product already has the same name")
    void should_throw_when_name_belongs_to_another_product() {
        // given
        UpdateProductCommand command = buildCommand();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct()));
        when(productRepository.existsByNameAndIdNot(command.name(), PRODUCT_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(PRODUCT_NAME);

        verify(productRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 6 — SKU duplicado en otro producto
    // =========================================================================

    @Test
    @DisplayName("execute throws when another product already has the same SKU")
    void should_throw_when_sku_belongs_to_another_product() {
        // given
        UpdateProductCommand command = buildCommand();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct()));
        when(productRepository.existsByNameAndIdNot(command.name(), PRODUCT_ID)).thenReturn(false);
        when(productRepository.existsBySkuAndIdNot(command.sku(), PRODUCT_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU");

        verify(productRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 7 — Cortocircuito: nombre duplicado => no verifica SKU ni persiste
    // =========================================================================

    @Test
    @DisplayName("execute never checks SKU or saves when name conflict is detected first")
    void should_not_check_sku_or_save_when_name_conflict_detected() {
        // given
        UpdateProductCommand command = buildCommand();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct()));
        when(productRepository.existsByNameAndIdNot(command.name(), PRODUCT_ID)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(productRepository, never()).existsBySkuAndIdNot(any(), any());
        verify(productRepository, never()).save(any());
    }

    // =========================================================================
    // GROUP 8 — El objeto correcto es pasado a save (ArgumentCaptor)
    // =========================================================================

    @Test
    @DisplayName("execute passes the mutated product instance to repository.save")
    void should_pass_mutated_product_to_save() {
        // given
        String imageUrl = "https://cdn.example.com/nuevo.jpg";
        UpdateProductCommand command = buildCommandWithImage(imageUrl);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct()));
        when(productRepository.existsByNameAndIdNot(command.name(), PRODUCT_ID)).thenReturn(false);
        when(productRepository.existsBySkuAndIdNot(command.sku(), PRODUCT_ID)).thenReturn(false);
        mockSavePassThrough();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

        // when
        useCase.execute(command);

        // then — verificar que el objeto pasado a save tiene los valores del command
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(PRODUCT_ID);
        assertThat(saved.getName()).isEqualTo(PRODUCT_NAME);
        assertThat(saved.getSku()).isEqualTo(PRODUCT_SKU);
        assertThat(saved.getImageUrl()).isEqualTo(imageUrl);
    }
}