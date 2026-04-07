package com.miapp.inventory_system.inventory.application.usecase;

import com.miapp.inventory_system.inventory.application.command.InitializeStockCommand;
import com.miapp.inventory_system.inventory.domain.model.Stock;
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
class InitializeStockUseCaseTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private InitializeStockUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers / constants
    // -------------------------------------------------------------------------

    private static final Long       PRODUCT_ID   = 1L;
    private static final Long       WAREHOUSE_ID = 2L;
    private static final BigDecimal MIN_QTY      = new BigDecimal("10.00");

    private InitializeStockCommand buildCommand() {
        return new InitializeStockCommand(PRODUCT_ID, WAREHOUSE_ID, MIN_QTY);
    }

    // =========================================================================
    // GROUP 1 — Happy path: stock no existe => se crea y guarda
    // =========================================================================

    @Test
    @DisplayName("execute creates and returns stock with quantity=ZERO when no prior stock exists")
    void should_initialize_stock_successfully_when_no_stock_exists() {
        // given
        when(stockRepository.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.empty());
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> {
            Stock s = inv.getArgument(0);
            return Stock.reconstitute(
                    10L,
                    s.getProductId(), s.getWarehouseId(),
                    s.getQuantity(), s.getMinQuantity(),
                    s.getUpdatedAt()
            );
        });

        // when
        Stock result = useCase.execute(buildCommand());

        // then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(result.getWarehouseId()).isEqualTo(WAREHOUSE_ID);
        assertThat(result.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getMinQuantity()).isEqualByComparingTo(MIN_QTY);
    }

    @Test
    @DisplayName("execute passes a stock with quantity=ZERO and correct ids to repository.save")
    void should_save_stock_with_zero_quantity_and_correct_ids() {
        // given
        when(stockRepository.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.empty());
        ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
        when(stockRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(buildCommand());

        // then — verificamos el objeto exacto que se pasó al repositorio
        Stock captured = captor.getValue();
        assertThat(captured.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(captured.getWarehouseId()).isEqualTo(WAREHOUSE_ID);
        assertThat(captured.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(captured.getMinQuantity()).isEqualByComparingTo(MIN_QTY);
        assertThat(captured.getUpdatedAt()).isNotNull();
    }

    // =========================================================================
    // GROUP 2 — Error de dominio: stock ya existe => lanza IllegalArgumentException
    // =========================================================================

    @Test
    @DisplayName("execute throws IllegalArgumentException when stock already exists for the product-warehouse pair")
    void should_throw_when_stock_already_exists() {
        // given
        Stock existingStock = Stock.reconstitute(
                5L, PRODUCT_ID, WAREHOUSE_ID,
                BigDecimal.ZERO, MIN_QTY, LocalDateTime.now()
        );
        when(stockRepository.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(existingStock));

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe stock inicializado");
    }

    // =========================================================================
    // GROUP 3 — Cortocircuito: stock ya existe => save nunca se invoca
    // =========================================================================

    @Test
    @DisplayName("execute never calls repository.save when stock already exists")
    void should_never_call_save_when_stock_already_exists() {
        // given
        Stock existingStock = Stock.reconstitute(
                5L, PRODUCT_ID, WAREHOUSE_ID,
                BigDecimal.ZERO, MIN_QTY, LocalDateTime.now()
        );
        when(stockRepository.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(existingStock));

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class);

        // then — cortocircuito: save no debe haberse llamado en ningún momento
        verify(stockRepository, never()).save(any());
    }
}
