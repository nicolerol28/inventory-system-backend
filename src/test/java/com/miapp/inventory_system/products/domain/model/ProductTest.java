package com.miapp.inventory_system.products.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    // -------------------------------------------------------------------------
    // Helpers: valores base válidos para reutilizar en múltiples tests
    // -------------------------------------------------------------------------

    private static final String VALID_NAME        = "Laptop Pro 15";
    private static final String VALID_DESCRIPTION = "Laptop de alto rendimiento";
    private static final String VALID_SKU         = "LAP-PRO-15";
    private static final Long   VALID_UNIT_ID     = 1L;
    private static final Long   VALID_CATEGORY_ID = 2L;
    private static final Long   VALID_SUPPLIER_ID = 3L;

    private static final Optional<BigDecimal> VALID_PURCHASE_PRICE =
            Optional.of(new BigDecimal("800.00"));
    private static final Optional<BigDecimal> VALID_SALE_PRICE =
            Optional.of(new BigDecimal("1200.00"));

    /** Crea un producto válido usando los valores base. */
    private Product buildValidProduct() {
        return Product.create(
                VALID_NAME, VALID_DESCRIPTION, VALID_SKU,
                VALID_UNIT_ID, VALID_CATEGORY_ID, VALID_SUPPLIER_ID,
                VALID_PURCHASE_PRICE, VALID_SALE_PRICE);
    }

    // =========================================================================
    // GROUP 1 — Product.create(...): happy path
    // =========================================================================

    @Test
    @DisplayName("create sets all fields correctly and initializes state")
    void should_create_product_with_correct_fields() {
        // given / when
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Product product = buildValidProduct();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // then — campos de negocio
        assertThat(product.getName()).isEqualTo(VALID_NAME);
        assertThat(product.getDescription()).isEqualTo(VALID_DESCRIPTION);
        assertThat(product.getSku()).isEqualTo(VALID_SKU);
        assertThat(product.getUnitId()).isEqualTo(VALID_UNIT_ID);
        assertThat(product.getCategoryId()).isEqualTo(VALID_CATEGORY_ID);
        assertThat(product.getSupplierId()).isEqualTo(VALID_SUPPLIER_ID);
        assertThat(product.getPurchasePrice()).isEqualTo(VALID_PURCHASE_PRICE);
        assertThat(product.getSalePrice()).isEqualTo(VALID_SALE_PRICE);

        // then — estado inicial
        assertThat(product.isActive()).isTrue();
        assertThat(product.getId()).isNull();
        assertThat(product.getImageUrl()).isNull();

        // then — timestamps dentro del rango de ejecución del test
        assertThat(product.getCreatedAt())
                .isNotNull()
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
        assertThat(product.getUpdatedAt())
                .isNotNull()
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("create accepts null supplierId (no business rule on supplier)")
    void should_create_product_when_supplier_id_is_null() {
        // given / when / then — supplierId es opcional por diseño
        Product product = Product.create(
                VALID_NAME, VALID_DESCRIPTION, VALID_SKU,
                VALID_UNIT_ID, VALID_CATEGORY_ID,
                null, // supplierId
                VALID_PURCHASE_PRICE, VALID_SALE_PRICE);

        assertThat(product.getSupplierId()).isNull();
        assertThat(product.isActive()).isTrue();
    }

    // =========================================================================
    // GROUP 2 — Product.create(...): campos obligatorios inválidos
    // =========================================================================

    static Stream<Arguments> invalidRequiredFieldsForCreate() {
        return Stream.of(
                Arguments.of(
                        null, VALID_SKU, VALID_UNIT_ID, VALID_CATEGORY_ID,
                        "El nombre del producto no puede estar vacío"),
                Arguments.of(
                        "   ", VALID_SKU, VALID_UNIT_ID, VALID_CATEGORY_ID,
                        "El nombre del producto no puede estar vacío"),
                Arguments.of(
                        VALID_NAME, null, VALID_UNIT_ID, VALID_CATEGORY_ID,
                        "El SKU es obligatorio"),
                Arguments.of(
                        VALID_NAME, "   ", VALID_UNIT_ID, VALID_CATEGORY_ID,
                        "El SKU es obligatorio"),
                Arguments.of(
                        VALID_NAME, VALID_SKU, null, VALID_CATEGORY_ID,
                        "La unidad es obligatoria"),
                Arguments.of(
                        VALID_NAME, VALID_SKU, VALID_UNIT_ID, null,
                        "La categoría es obligatoria")
        );
    }

    @ParameterizedTest(name = "[{index}] {4}")
    @MethodSource("invalidRequiredFieldsForCreate")
    @DisplayName("create throws IllegalArgumentException for each invalid required field")
    void should_throw_when_required_fields_are_invalid_on_create(
            String name, String sku, Long unitId, Long categoryId,
            String expectedMessage) {

        assertThatThrownBy(() -> Product.create(
                name, VALID_DESCRIPTION, sku,
                unitId, categoryId, VALID_SUPPLIER_ID,
                VALID_PURCHASE_PRICE, VALID_SALE_PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    // =========================================================================
    // GROUP 3 — Product.create(...): validaciones de precio de compra
    // =========================================================================

    @Test
    @DisplayName("create throws when purchasePrice is zero")
    void should_throw_when_purchase_price_is_zero() {
        assertThatThrownBy(() -> Product.create(
                VALID_NAME, VALID_DESCRIPTION, VALID_SKU,
                VALID_UNIT_ID, VALID_CATEGORY_ID, VALID_SUPPLIER_ID,
                Optional.of(BigDecimal.ZERO),
                VALID_SALE_PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El precio de compra debe ser mayor a cero");
    }

    @Test
    @DisplayName("create throws when purchasePrice is negative")
    void should_throw_when_purchase_price_is_negative() {
        assertThatThrownBy(() -> Product.create(
                VALID_NAME, VALID_DESCRIPTION, VALID_SKU,
                VALID_UNIT_ID, VALID_CATEGORY_ID, VALID_SUPPLIER_ID,
                Optional.of(new BigDecimal("-0.01")),
                VALID_SALE_PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El precio de compra debe ser mayor a cero");
    }

    // =========================================================================
    // GROUP 4 — Product.create(...): validaciones de precio de venta
    // =========================================================================

    @Test
    @DisplayName("create throws when salePrice is zero")
    void should_throw_when_sale_price_is_zero() {
        assertThatThrownBy(() -> Product.create(
                VALID_NAME, VALID_DESCRIPTION, VALID_SKU,
                VALID_UNIT_ID, VALID_CATEGORY_ID, VALID_SUPPLIER_ID,
                VALID_PURCHASE_PRICE,
                Optional.of(BigDecimal.ZERO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El precio de venta debe ser mayor a cero");
    }

    @Test
    @DisplayName("create throws when salePrice is negative")
    void should_throw_when_sale_price_is_negative() {
        assertThatThrownBy(() -> Product.create(
                VALID_NAME, VALID_DESCRIPTION, VALID_SKU,
                VALID_UNIT_ID, VALID_CATEGORY_ID, VALID_SUPPLIER_ID,
                VALID_PURCHASE_PRICE,
                Optional.of(new BigDecimal("-50.00"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El precio de venta debe ser mayor a cero");
    }

    // =========================================================================
    // GROUP 5 — Product.create(...): precios vacíos son válidos
    // =========================================================================

    @Test
    @DisplayName("create succeeds when both prices are Optional.empty()")
    void should_create_product_when_both_prices_are_empty() {
        // given / when
        Product product = Product.create(
                VALID_NAME, VALID_DESCRIPTION, VALID_SKU,
                VALID_UNIT_ID, VALID_CATEGORY_ID, VALID_SUPPLIER_ID,
                Optional.empty(),
                Optional.empty());

        // then — no lanza excepción y los precios quedan vacíos
        assertThat(product.getPurchasePrice()).isEmpty();
        assertThat(product.getSalePrice()).isEmpty();
        assertThat(product.isActive()).isTrue();
    }

    // =========================================================================
    // GROUP 6 — Product.reconstitute(...): mapeo exacto sin validaciones
    // =========================================================================

    @Test
    @DisplayName("reconstitute maps all fields exactly and skips business validation")
    void should_reconstitute_product_with_all_fields_mapped_exactly() {
        // given
        Long         id          = 99L;
        String       name        = "Producto Archivado";
        String       description = "Descripción archivada";
        String       sku         = "SKU-ARCH-001";
        Long         unitId      = 10L;
        Long         categoryId  = 20L;
        Long         supplierId  = 30L;
        BigDecimal   purchase    = new BigDecimal("5.00");
        BigDecimal   sale        = new BigDecimal("10.00");
        boolean      active      = false;  // confirma que reconstitute no valida estado
        LocalDateTime createdAt  = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime updatedAt  = LocalDateTime.of(2024, 6, 20, 18, 30);
        String       imageUrl    = "https://cdn.example.com/img/producto.jpg";

        // when
        Product product = Product.reconstitute(
                id, name, description, sku,
                unitId, categoryId, supplierId,
                Optional.of(purchase), Optional.of(sale),
                active, createdAt, updatedAt, imageUrl);

        // then — cada campo debe coincidir exactamente con el parámetro pasado
        assertThat(product.getId()).isEqualTo(id);
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getDescription()).isEqualTo(description);
        assertThat(product.getSku()).isEqualTo(sku);
        assertThat(product.getUnitId()).isEqualTo(unitId);
        assertThat(product.getCategoryId()).isEqualTo(categoryId);
        assertThat(product.getSupplierId()).isEqualTo(supplierId);
        assertThat(product.getPurchasePrice()).contains(purchase);
        assertThat(product.getSalePrice()).contains(sale);
        assertThat(product.isActive()).isFalse();
        assertThat(product.getCreatedAt()).isEqualTo(createdAt);
        assertThat(product.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(product.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("reconstitute accepts empty prices and null imageUrl without throwing")
    void should_reconstitute_product_with_empty_prices_and_null_image() {
        // given / when
        Product product = Product.reconstitute(
                1L, "Nombre", null, "SKU-001",
                1L, 1L, null,
                Optional.empty(), Optional.empty(),
                true,
                LocalDateTime.now(), LocalDateTime.now(),
                null);

        // then — sin excepción, precios vacíos mapeados tal cual
        assertThat(product.getPurchasePrice()).isEmpty();
        assertThat(product.getSalePrice()).isEmpty();
        assertThat(product.getImageUrl()).isNull();
    }

    // =========================================================================
    // GROUP 7 — Product.update(...): happy path
    // =========================================================================

    @Test
    @DisplayName("update changes all fields and refreshes updatedAt")
    void should_update_product_fields_and_refresh_updated_at() throws InterruptedException {
        // given
        Product product = buildValidProduct();
        LocalDateTime originalUpdatedAt = product.getUpdatedAt();

        // Pausa mínima para garantizar que el nuevo timestamp sea posterior
        Thread.sleep(10);

        String       newName        = "Monitor UltraWide";
        String       newDescription = "Monitor curvo 34 pulgadas";
        String       newSku         = "MON-UW-34";
        Long         newUnitId      = 5L;
        Long         newCategoryId  = 6L;
        Long         newSupplierId  = 7L;
        BigDecimal   newPurchase    = new BigDecimal("300.00");
        BigDecimal   newSale        = new BigDecimal("450.00");

        // when
        product.update(
                newName, newDescription, newSku,
                newUnitId, newCategoryId, newSupplierId,
                Optional.of(newPurchase), Optional.of(newSale));

        // then — campos actualizados
        assertThat(product.getName()).isEqualTo(newName);
        assertThat(product.getDescription()).isEqualTo(newDescription);
        assertThat(product.getSku()).isEqualTo(newSku);
        assertThat(product.getUnitId()).isEqualTo(newUnitId);
        assertThat(product.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(product.getSupplierId()).isEqualTo(newSupplierId);
        assertThat(product.getPurchasePrice()).contains(newPurchase);
        assertThat(product.getSalePrice()).contains(newSale);

        // then — updatedAt renovado
        assertThat(product.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    // =========================================================================
    // GROUP 8 — Product.update(...): campos obligatorios inválidos
    // =========================================================================

    static Stream<Arguments> invalidRequiredFieldsForUpdate() {
        return Stream.of(
                Arguments.of(
                        null, VALID_SKU, VALID_UNIT_ID, VALID_CATEGORY_ID,
                        "El nombre del producto no puede estar vacío"),
                Arguments.of(
                        "   ", VALID_SKU, VALID_UNIT_ID, VALID_CATEGORY_ID,
                        "El nombre del producto no puede estar vacío"),
                Arguments.of(
                        VALID_NAME, null, VALID_UNIT_ID, VALID_CATEGORY_ID,
                        "El SKU es obligatorio"),
                Arguments.of(
                        VALID_NAME, "   ", VALID_UNIT_ID, VALID_CATEGORY_ID,
                        "El SKU es obligatorio"),
                Arguments.of(
                        VALID_NAME, VALID_SKU, null, VALID_CATEGORY_ID,
                        "La unidad es obligatoria"),
                Arguments.of(
                        VALID_NAME, VALID_SKU, VALID_UNIT_ID, null,
                        "La categoría es obligatoria")
        );
    }

    @ParameterizedTest(name = "[{index}] {4}")
    @MethodSource("invalidRequiredFieldsForUpdate")
    @DisplayName("update throws IllegalArgumentException for each invalid required field")
    void should_throw_when_required_fields_are_invalid_on_update(
            String name, String sku, Long unitId, Long categoryId,
            String expectedMessage) {

        // given — instancia válida preexistente
        Product product = buildValidProduct();

        // when / then
        assertThatThrownBy(() -> product.update(
                name, VALID_DESCRIPTION, sku,
                unitId, categoryId, VALID_SUPPLIER_ID,
                VALID_PURCHASE_PRICE, VALID_SALE_PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    // =========================================================================
    // GROUP 9 — Product.updateImage(...): happy path
    // =========================================================================

    @Test
    @DisplayName("updateImage sets imageUrl and refreshes updatedAt")
    void should_update_image_url_and_refresh_updated_at() throws InterruptedException {
        // given
        Product product = buildValidProduct();
        LocalDateTime originalUpdatedAt = product.getUpdatedAt();
        Thread.sleep(10);

        String newImageUrl = "https://cdn.example.com/images/laptop-pro-15.webp";

        // when
        product.updateImage(newImageUrl);

        // then
        assertThat(product.getImageUrl()).isEqualTo(newImageUrl);
        assertThat(product.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("updateImage replaces a previously set imageUrl")
    void should_replace_existing_image_url() {
        // given — producto con imagen previa (vía reconstitute)
        Product product = Product.reconstitute(
                1L, VALID_NAME, VALID_DESCRIPTION, VALID_SKU,
                VALID_UNIT_ID, VALID_CATEGORY_ID, VALID_SUPPLIER_ID,
                VALID_PURCHASE_PRICE, VALID_SALE_PRICE,
                true, LocalDateTime.now(), LocalDateTime.now(),
                "https://cdn.example.com/old-image.jpg");

        String newImageUrl = "https://cdn.example.com/new-image.jpg";

        // when
        product.updateImage(newImageUrl);

        // then
        assertThat(product.getImageUrl()).isEqualTo(newImageUrl);
    }

    // =========================================================================
    // GROUP 10 — Product.deactivate(): happy path y estado ya inactivo
    // =========================================================================

    @Test
    @DisplayName("deactivate sets active to false on an active product")
    void should_deactivate_active_product() {
        // given
        Product product = buildValidProduct();
        assertThat(product.isActive()).isTrue();

        // when
        product.deactivate();

        // then
        assertThat(product.isActive()).isFalse();
    }

    @Test
    @DisplayName("deactivate throws when product is already inactive")
    void should_throw_when_deactivating_already_inactive_product() {
        // given — producto reconstituido con active=false
        Product product = Product.reconstitute(
                1L, VALID_NAME, VALID_DESCRIPTION, VALID_SKU,
                VALID_UNIT_ID, VALID_CATEGORY_ID, VALID_SUPPLIER_ID,
                VALID_PURCHASE_PRICE, VALID_SALE_PRICE,
                false, LocalDateTime.now(), LocalDateTime.now(), null);

        // when / then
        assertThatThrownBy(product::deactivate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El producto ya está desactivado");
    }
}
