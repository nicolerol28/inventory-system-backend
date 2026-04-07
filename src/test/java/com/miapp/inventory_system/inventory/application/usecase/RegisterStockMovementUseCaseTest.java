package com.miapp.inventory_system.inventory.application.usecase;

import com.miapp.inventory_system.inventory.application.command.RegisterStockMovementCommand;
import com.miapp.inventory_system.inventory.domain.model.InventoryMovement;
import com.miapp.inventory_system.inventory.domain.model.MovementType;
import com.miapp.inventory_system.inventory.domain.model.Stock;
import com.miapp.inventory_system.inventory.domain.repository.InventoryMovementRepository;
import com.miapp.inventory_system.inventory.domain.repository.StockRepository;
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
class RegisterStockMovementUseCaseTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @InjectMocks
    private RegisterStockMovementUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers / constants
    // -------------------------------------------------------------------------

    private static final Long       PRODUCT_ID    = 1L;
    private static final Long       WAREHOUSE_ID  = 2L;
    private static final Long       SUPPLIER_ID   = 10L;
    private static final Long       REGISTERED_BY = 99L;
    private static final BigDecimal CURRENT_STOCK = new BigDecimal("100.00");
    private static final BigDecimal MOVEMENT_QTY  = new BigDecimal("30.00");

    private Stock buildExistingStock(BigDecimal quantity) {
        return Stock.reconstitute(
                5L, PRODUCT_ID, WAREHOUSE_ID,
                quantity, BigDecimal.ZERO, LocalDateTime.now()
        );
    }

    private RegisterStockMovementCommand buildCommand(MovementType type, BigDecimal qty) {
        return new RegisterStockMovementCommand(
                PRODUCT_ID, WAREHOUSE_ID, SUPPLIER_ID, REGISTERED_BY,
                type, qty, "test comment"
        );
    }

    private InventoryMovement buildSavedMovement(MovementType type, BigDecimal qty,
                                                  BigDecimal before, BigDecimal after) {
        return InventoryMovement.reconstitute(
                100L, PRODUCT_ID, WAREHOUSE_ID, SUPPLIER_ID, REGISTERED_BY,
                type, qty, before, after, "test comment", LocalDateTime.now()
        );
    }

    // =========================================================================
    // GROUP 1 — Happy path: inbound con stock existente
    // =========================================================================

    @Test
    @DisplayName("execute registers inbound movement, updates stock, and saves both when stock exists")
    void should_register_inbound_movement_with_existing_stock() {
        // given
        Stock existingStock = buildExistingStock(CURRENT_STOCK);
        BigDecimal expectedAfter = CURRENT_STOCK.add(MOVEMENT_QTY); // 130

        when(stockRepository.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(existingStock));

        InventoryMovement savedMovement = buildSavedMovement(
                MovementType.PURCHASE_ENTRY, MOVEMENT_QTY, CURRENT_STOCK, expectedAfter);
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenReturn(savedMovement);
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        InventoryMovement result = useCase.execute(
                buildCommand(MovementType.PURCHASE_ENTRY, MOVEMENT_QTY));

        // then
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getQuantityAfter()).isEqualByComparingTo(expectedAfter);
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("execute saves movement with correct quantityBefore and quantityAfter for inbound")
    void should_capture_correct_quantities_in_saved_movement_for_inbound() {
        // given
        Stock existingStock = buildExistingStock(CURRENT_STOCK);
        when(stockRepository.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(existingStock));

        ArgumentCaptor<InventoryMovement> movementCaptor =
                ArgumentCaptor.forClass(InventoryMovement.class);
        when(inventoryMovementRepository.save(movementCaptor.capture()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(buildCommand(MovementType.PURCHASE_ENTRY, MOVEMENT_QTY));

        // then — validamos exactamente el objeto que se pasó al repositorio de movimientos
        InventoryMovement captured = movementCaptor.getValue();
        assertThat(captured.getQuantityBefore()).isEqualByComparingTo(CURRENT_STOCK);
        assertThat(captured.getQuantityAfter()).isEqualByComparingTo(
                CURRENT_STOCK.add(MOVEMENT_QTY));
        assertThat(captured.getMovementType()).isEqualTo(MovementType.PURCHASE_ENTRY);
    }

    // =========================================================================
    // GROUP 2 — Happy path: inbound sin stock => se crea stock nuevo
    // =========================================================================

    @Test
    @DisplayName("execute creates new stock when no prior stock exists for an inbound movement")
    void should_create_new_stock_and_register_inbound_movement_when_no_stock_exists() {
        // given — no existe stock, el use case lo crea con quantity=ZERO
        when(stockRepository.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.empty());

        // primera llamada a save (el stock nuevo), segunda llamada (después de apply)
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> {
            Stock s = inv.getArgument(0);
            return Stock.reconstitute(
                    55L, s.getProductId(), s.getWarehouseId(),
                    s.getQuantity(), s.getMinQuantity(), s.getUpdatedAt()
            );
        });

        InventoryMovement savedMovement = buildSavedMovement(
                MovementType.PURCHASE_ENTRY, MOVEMENT_QTY,
                BigDecimal.ZERO, MOVEMENT_QTY);
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenReturn(savedMovement);

        // when
        InventoryMovement result = useCase.execute(
                buildCommand(MovementType.PURCHASE_ENTRY, MOVEMENT_QTY));

        // then — el movimiento retornado debe reflejar el stock inicial en cero
        assertThat(result.getQuantityBefore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getQuantityAfter()).isEqualByComparingTo(MOVEMENT_QTY);
        // stockRepository.save debe haberse llamado dos veces: creación + update tras apply
        verify(stockRepository, times(2)).save(any(Stock.class));
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    // =========================================================================
    // GROUP 3 — Happy path: outbound con stock existente
    // =========================================================================

    @Test
    @DisplayName("execute registers outbound movement and reduces stock quantity correctly")
    void should_register_outbound_movement_and_reduce_stock() {
        // given
        Stock existingStock = buildExistingStock(CURRENT_STOCK);
        BigDecimal expectedAfter = CURRENT_STOCK.subtract(MOVEMENT_QTY); // 70

        when(stockRepository.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(existingStock));

        ArgumentCaptor<InventoryMovement> movementCaptor =
                ArgumentCaptor.forClass(InventoryMovement.class);
        when(inventoryMovementRepository.save(movementCaptor.capture()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(buildCommand(MovementType.SALE_EXIT, MOVEMENT_QTY));

        // then
        InventoryMovement captured = movementCaptor.getValue();
        assertThat(captured.getMovementType()).isEqualTo(MovementType.SALE_EXIT);
        assertThat(captured.getQuantityBefore()).isEqualByComparingTo(CURRENT_STOCK);
        assertThat(captured.getQuantityAfter()).isEqualByComparingTo(expectedAfter);
        verify(stockRepository).save(any(Stock.class));
    }

    // =========================================================================
    // GROUP 4 — Error: outbound sin stock existente => IllegalArgumentException
    // =========================================================================

    @Test
    @DisplayName("execute throws IllegalArgumentException when outbound movement is requested but no stock exists")
    void should_throw_when_outbound_movement_requested_and_no_stock_exists() {
        // given
        when(stockRepository.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(buildCommand(MovementType.SALE_EXIT, MOVEMENT_QTY))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No existe stock");
    }

    // =========================================================================
    // GROUP 5 — Cortocircuito: outbound sin stock => movement repository nunca se llama
    // =========================================================================

    @Test
    @DisplayName("execute never calls inventoryMovementRepository.save when outbound movement has no stock")
    void should_never_save_movement_when_outbound_has_no_stock() {
        // given
        when(stockRepository.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() ->
                useCase.execute(buildCommand(MovementType.SALE_EXIT, MOVEMENT_QTY))
        )
                .isInstanceOf(IllegalArgumentException.class);

        // then — cortocircuito: save del movimiento nunca debe invocarse
        verify(inventoryMovementRepository, never()).save(any());
    }
}
