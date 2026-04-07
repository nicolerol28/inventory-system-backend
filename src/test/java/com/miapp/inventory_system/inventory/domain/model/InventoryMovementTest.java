package com.miapp.inventory_system.inventory.domain.model;

import com.miapp.inventory_system.shared.exception.InsufficientStockException;
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

class InventoryMovementTest {

    // -------------------------------------------------------------------------
    // Helpers / constants
    // -------------------------------------------------------------------------

    private static final Long       PRODUCT_ID    = 1L;
    private static final Long       WAREHOUSE_ID  = 2L;
    private static final Long       SUPPLIER_ID   = 10L;
    private static final Long       REGISTERED_BY = 99L;
    private static final String     COMMENT       = "unit test movement";
    private static final BigDecimal CURRENT_STOCK = new BigDecimal("100.00");
    private static final BigDecimal QUANTITY       = new BigDecimal("30.00");

    private InventoryMovement createInbound(BigDecimal qty, BigDecimal stock) {
        return InventoryMovement.create(
                PRODUCT_ID, WAREHOUSE_ID, SUPPLIER_ID, REGISTERED_BY,
                MovementType.PURCHASE_ENTRY, qty, stock, COMMENT
        );
    }

    private InventoryMovement createOutbound(BigDecimal qty, BigDecimal stock) {
        return InventoryMovement.create(
                PRODUCT_ID, WAREHOUSE_ID, null, REGISTERED_BY,
                MovementType.SALE_EXIT, qty, stock, COMMENT
        );
    }

    // =========================================================================
    // GROUP 1 — InventoryMovement.create(...): happy path inbound
    // =========================================================================

    @Test
    @DisplayName("create PURCHASE_ENTRY sets quantityAfter = currentStock + quantity")
    void should_calculate_quantity_after_correctly_for_inbound_movement() {
        // given
        BigDecimal expectedAfter = CURRENT_STOCK.add(QUANTITY); // 130

        // when
        InventoryMovement movement = createInbound(QUANTITY, CURRENT_STOCK);

        // then
        assertThat(movement.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(movement.getWarehouseId()).isEqualTo(WAREHOUSE_ID);
        assertThat(movement.getSupplierId()).isEqualTo(SUPPLIER_ID);
        assertThat(movement.getRegisteredBy()).isEqualTo(REGISTERED_BY);
        assertThat(movement.getMovementType()).isEqualTo(MovementType.PURCHASE_ENTRY);
        assertThat(movement.getQuantity()).isEqualByComparingTo(QUANTITY);
        assertThat(movement.getQuantityBefore()).isEqualByComparingTo(CURRENT_STOCK);
        assertThat(movement.getQuantityAfter()).isEqualByComparingTo(expectedAfter);
        assertThat(movement.getCreatedAt()).isNotNull();
    }

    // =========================================================================
    // GROUP 2 — InventoryMovement.create(...): happy path outbound
    // =========================================================================

    @Test
    @DisplayName("create SALE_EXIT sets quantityAfter = currentStock - quantity")
    void should_calculate_quantity_after_correctly_for_outbound_movement() {
        // given
        BigDecimal expectedAfter = CURRENT_STOCK.subtract(QUANTITY); // 70

        // when
        InventoryMovement movement = createOutbound(QUANTITY, CURRENT_STOCK);

        // then
        assertThat(movement.getQuantityBefore()).isEqualByComparingTo(CURRENT_STOCK);
        assertThat(movement.getQuantityAfter()).isEqualByComparingTo(expectedAfter);
    }

    @Test
    @DisplayName("create outbound does not throw when quantity exactly equals currentStock (boundary)")
    void should_not_throw_when_outbound_quantity_equals_current_stock() {
        // given — salida exactamente igual al stock disponible
        BigDecimal exactQty = new BigDecimal("100.00");

        // when
        InventoryMovement movement = createOutbound(exactQty, CURRENT_STOCK);

        // then — quantityAfter should be zero
        assertThat(movement.getQuantityAfter()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // =========================================================================
    // GROUP 3 — InventoryMovement.create(...): quantity inválida
    // =========================================================================

    static Stream<Arguments> invalidQuantityValues() {
        return Stream.of(
                Arguments.of((BigDecimal) null,        "La cantidad del movimiento debe ser mayor a cero"),
                Arguments.of(BigDecimal.ZERO,          "La cantidad del movimiento debe ser mayor a cero"),
                Arguments.of(new BigDecimal("-1.00"),  "La cantidad del movimiento debe ser mayor a cero")
        );
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("invalidQuantityValues")
    @DisplayName("create throws IllegalArgumentException for each invalid quantity value")
    void should_throw_when_quantity_is_invalid(BigDecimal invalidQty, String expectedMessage) {
        assertThatThrownBy(() -> InventoryMovement.create(
                PRODUCT_ID, WAREHOUSE_ID, SUPPLIER_ID, REGISTERED_BY,
                MovementType.PURCHASE_ENTRY, invalidQty, CURRENT_STOCK, COMMENT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    // =========================================================================
    // GROUP 4 — InventoryMovement.create(...): stock insuficiente en salida
    // =========================================================================

    @Test
    @DisplayName("create throws InsufficientStockException when outbound quantity exceeds currentStock")
    void should_throw_insufficient_stock_exception_when_outbound_exceeds_stock() {
        // given
        BigDecimal excessQty = new BigDecimal("150.00"); // 150 > 100

        // when / then
        assertThatThrownBy(() -> createOutbound(excessQty, CURRENT_STOCK))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    @DisplayName("create does NOT throw InsufficientStockException for inbound regardless of stock")
    void should_not_throw_insufficient_stock_for_inbound_movement() {
        // given — stock is zero but movement is inbound, so no restriction applies
        BigDecimal largeQty = new BigDecimal("9999.00");

        // when / then — must not throw
        InventoryMovement movement = createInbound(largeQty, BigDecimal.ZERO);
        assertThat(movement.getQuantityAfter()).isEqualByComparingTo(largeQty);
    }

    // =========================================================================
    // GROUP 5 — InventoryMovement.reconstitute(...): campo a campo, sin validaciones
    // =========================================================================

    @Test
    @DisplayName("reconstitute maps all fields exactly without any validation")
    void should_reconstitute_movement_with_all_fields_preserved() {
        // given
        Long          id             = 77L;
        BigDecimal    qty            = new BigDecimal("50.00");
        BigDecimal    qtyBefore      = new BigDecimal("200.00");
        BigDecimal    qtyAfter       = new BigDecimal("250.00");
        String        comment        = "reconstitution test";
        LocalDateTime createdAt      = LocalDateTime.of(2024, 6, 1, 12, 0);

        // when
        InventoryMovement movement = InventoryMovement.reconstitute(
                id, PRODUCT_ID, WAREHOUSE_ID, SUPPLIER_ID, REGISTERED_BY,
                MovementType.RETURN_ENTRY, qty, qtyBefore, qtyAfter, comment, createdAt
        );

        // then
        assertThat(movement.getId()).isEqualTo(id);
        assertThat(movement.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(movement.getWarehouseId()).isEqualTo(WAREHOUSE_ID);
        assertThat(movement.getSupplierId()).isEqualTo(SUPPLIER_ID);
        assertThat(movement.getRegisteredBy()).isEqualTo(REGISTERED_BY);
        assertThat(movement.getMovementType()).isEqualTo(MovementType.RETURN_ENTRY);
        assertThat(movement.getQuantity()).isEqualByComparingTo(qty);
        assertThat(movement.getQuantityBefore()).isEqualByComparingTo(qtyBefore);
        assertThat(movement.getQuantityAfter()).isEqualByComparingTo(qtyAfter);
        assertThat(movement.getComment()).isEqualTo(comment);
        assertThat(movement.getCreatedAt()).isEqualTo(createdAt);
    }

    // =========================================================================
    // GROUP 6 — MovementType.isInbound(): todos los valores del enum
    // =========================================================================

    static Stream<Arguments> movementTypeInboundValues() {
        return Stream.of(
                Arguments.of(MovementType.PURCHASE_ENTRY,  true),
                Arguments.of(MovementType.RETURN_ENTRY,    true),
                Arguments.of(MovementType.ADJUSTMENT_IN,   true),
                Arguments.of(MovementType.SALE_EXIT,       false),
                Arguments.of(MovementType.DAMAGE_EXIT,     false),
                Arguments.of(MovementType.ADJUSTMENT_OUT,  false)
        );
    }

    @ParameterizedTest(name = "[{index}] {0} => isInbound={1}")
    @MethodSource("movementTypeInboundValues")
    @DisplayName("MovementType.isInbound() returns the correct value for each enum constant")
    void should_return_correct_inbound_flag_for_each_movement_type(
            MovementType movementType, boolean expectedInbound) {
        assertThat(movementType.isInbound()).isEqualTo(expectedInbound);
    }
}
