package com.miapp.inventory_system.inventory.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    // -------------------------------------------------------------------------
    // Helpers / constants
    // -------------------------------------------------------------------------

    private static final Long       PRODUCT_ID   = 1L;
    private static final Long       WAREHOUSE_ID = 2L;
    private static final BigDecimal MIN_QTY      = new BigDecimal("10.00");

    /** Creates a valid inbound movement that matches PRODUCT_ID / WAREHOUSE_ID. */
    private InventoryMovement buildInboundMovement(BigDecimal quantity, BigDecimal currentStock) {
        return InventoryMovement.create(
                PRODUCT_ID, WAREHOUSE_ID,
                null, 99L,
                MovementType.PURCHASE_ENTRY,
                quantity, currentStock,
                "test movement"
        );
    }

    // =========================================================================
    // GROUP 1 — Stock.create(...): happy path
    // =========================================================================

    @Test
    @DisplayName("create sets quantity=ZERO, correct minQuantity, updatedAt not null, and id=null")
    void should_create_stock_with_zero_quantity_and_not_null_timestamp() {
        // given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // when
        Stock stock = Stock.create(PRODUCT_ID, WAREHOUSE_ID, MIN_QTY);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // then
        assertThat(stock.getId()).isNull();
        assertThat(stock.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(stock.getWarehouseId()).isEqualTo(WAREHOUSE_ID);
        assertThat(stock.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stock.getMinQuantity()).isEqualByComparingTo(MIN_QTY);
        assertThat(stock.getUpdatedAt())
                .isNotNull()
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    // =========================================================================
    // GROUP 2 — Stock.reconstitute(...): campo a campo sin validaciones
    // =========================================================================

    @Test
    @DisplayName("reconstitute maps all fields exactly without applying any validation")
    void should_reconstitute_stock_with_all_fields_preserved() {
        // given
        Long          id          = 42L;
        BigDecimal    quantity    = new BigDecimal("100.50");
        BigDecimal    minQuantity = new BigDecimal("5.00");
        LocalDateTime updatedAt   = LocalDateTime.of(2024, 3, 15, 10, 30);

        // when
        Stock stock = Stock.reconstitute(id, PRODUCT_ID, WAREHOUSE_ID, quantity, minQuantity, updatedAt);

        // then — every field must match exactly what was passed in
        assertThat(stock.getId()).isEqualTo(id);
        assertThat(stock.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(stock.getWarehouseId()).isEqualTo(WAREHOUSE_ID);
        assertThat(stock.getQuantity()).isEqualByComparingTo(quantity);
        assertThat(stock.getMinQuantity()).isEqualByComparingTo(minQuantity);
        assertThat(stock.getUpdatedAt()).isEqualTo(updatedAt);
    }

    // =========================================================================
    // GROUP 3 — Stock.apply(movement): actualización de cantidad
    // =========================================================================

    @Test
    @DisplayName("apply updates quantity to movement's quantityAfter and refreshes updatedAt")
    void should_apply_movement_and_update_quantity() {
        // given
        Stock stock = Stock.create(PRODUCT_ID, WAREHOUSE_ID, MIN_QTY);
        BigDecimal movementQty   = new BigDecimal("25.00");
        BigDecimal expectedAfter = movementQty; // currentStock=0 + 25 = 25
        InventoryMovement movement = buildInboundMovement(movementQty, BigDecimal.ZERO);

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // when
        stock.apply(movement);

        // then
        assertThat(stock.getQuantity()).isEqualByComparingTo(expectedAfter);
        assertThat(stock.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("apply throws when movement productId does not match stock productId")
    void should_throw_when_movement_product_id_does_not_match() {
        // given
        Stock stock = Stock.create(PRODUCT_ID, WAREHOUSE_ID, MIN_QTY);
        InventoryMovement movement = InventoryMovement.create(
                999L, WAREHOUSE_ID,   // productId differs
                null, 99L,
                MovementType.PURCHASE_ENTRY,
                new BigDecimal("10.00"), BigDecimal.ZERO,
                "wrong product"
        );

        // when / then
        assertThatThrownBy(() -> stock.apply(movement))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("movimiento no corresponde");
    }

    @Test
    @DisplayName("apply throws when movement warehouseId does not match stock warehouseId")
    void should_throw_when_movement_warehouse_id_does_not_match() {
        // given
        Stock stock = Stock.create(PRODUCT_ID, WAREHOUSE_ID, MIN_QTY);
        InventoryMovement movement = InventoryMovement.create(
                PRODUCT_ID, 999L,     // warehouseId differs
                null, 99L,
                MovementType.PURCHASE_ENTRY,
                new BigDecimal("10.00"), BigDecimal.ZERO,
                "wrong warehouse"
        );

        // when / then
        assertThatThrownBy(() -> stock.apply(movement))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("movimiento no corresponde");
    }

    // =========================================================================
    // GROUP 4 — Stock.updateMinQuantity(...): happy path e inputs inválidos
    // =========================================================================

    @Test
    @DisplayName("updateMinQuantity updates minQuantity and refreshes updatedAt")
    void should_update_min_quantity_successfully() {
        // given
        Stock stock = Stock.create(PRODUCT_ID, WAREHOUSE_ID, MIN_QTY);
        BigDecimal newMin = new BigDecimal("20.00");
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // when
        stock.updateMinQuantity(newMin);

        // then
        assertThat(stock.getMinQuantity()).isEqualByComparingTo(newMin);
        assertThat(stock.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("updateMinQuantity accepts zero as a valid minimum quantity")
    void should_update_min_quantity_when_value_is_zero() {
        // given
        Stock stock = Stock.create(PRODUCT_ID, WAREHOUSE_ID, MIN_QTY);

        // when
        stock.updateMinQuantity(BigDecimal.ZERO);

        // then
        assertThat(stock.getMinQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    static Stream<Arguments> invalidMinQuantityValues() {
        return Stream.of(
                Arguments.of((BigDecimal) null,              "La cantidad mínima no puede ser negativa"),
                Arguments.of(new BigDecimal("-0.01"),        "La cantidad mínima no puede ser negativa"),
                Arguments.of(new BigDecimal("-100"),         "La cantidad mínima no puede ser negativa")
        );
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("invalidMinQuantityValues")
    @DisplayName("updateMinQuantity throws IllegalArgumentException for each invalid value")
    void should_throw_when_min_quantity_is_invalid(BigDecimal invalidValue, String expectedMessage) {
        // given
        Stock stock = Stock.create(PRODUCT_ID, WAREHOUSE_ID, MIN_QTY);

        // when / then
        assertThatThrownBy(() -> stock.updateMinQuantity(invalidValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    // =========================================================================
    // GROUP 5 — Stock.isBelowMinimum(): lógica comparativa
    // =========================================================================

    @Test
    @DisplayName("isBelowMinimum returns true when quantity is strictly less than minQuantity")
    void should_return_true_when_quantity_is_below_minimum() {
        // given — stock with quantity < minQuantity after reconstitution
        Stock stock = Stock.reconstitute(
                1L, PRODUCT_ID, WAREHOUSE_ID,
                new BigDecimal("5.00"),   // quantity
                new BigDecimal("10.00"),  // minQuantity
                LocalDateTime.now()
        );

        // when / then
        assertThat(stock.isBelowMinimum()).isTrue();
    }

    @Test
    @DisplayName("isBelowMinimum returns false when quantity equals minQuantity")
    void should_return_false_when_quantity_equals_minimum() {
        // given
        BigDecimal same = new BigDecimal("10.00");
        Stock stock = Stock.reconstitute(1L, PRODUCT_ID, WAREHOUSE_ID, same, same, LocalDateTime.now());

        // when / then
        assertThat(stock.isBelowMinimum()).isFalse();
    }

    @Test
    @DisplayName("isBelowMinimum returns false when quantity is greater than minQuantity")
    void should_return_false_when_quantity_is_above_minimum() {
        // given
        Stock stock = Stock.reconstitute(
                1L, PRODUCT_ID, WAREHOUSE_ID,
                new BigDecimal("50.00"),  // quantity
                new BigDecimal("10.00"),  // minQuantity
                LocalDateTime.now()
        );

        // when / then
        assertThat(stock.isBelowMinimum()).isFalse();
    }
}
