package com.miapp.inventory_system.inventory.application.usecase;

import com.miapp.inventory_system.inventory.domain.model.Stock;
import com.miapp.inventory_system.inventory.domain.repository.StockRepository;
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
class UpdateMinQuantityUseCaseTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private UpdateMinQuantityUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers / constants
    // -------------------------------------------------------------------------

    private static final Long       STOCK_ID     = 5L;
    private static final Long       PRODUCT_ID   = 1L;
    private static final Long       WAREHOUSE_ID = 2L;
    private static final BigDecimal OLD_MIN_QTY  = new BigDecimal("10.00");
    private static final BigDecimal NEW_MIN_QTY  = new BigDecimal("25.00");

    private Stock buildExistingStock() {
        return Stock.reconstitute(
                STOCK_ID, PRODUCT_ID, WAREHOUSE_ID,
                new BigDecimal("50.00"), OLD_MIN_QTY, LocalDateTime.now()
        );
    }

    // =========================================================================
    // GROUP 1 — Happy path: stock existe => minQuantity actualizada y stock guardado
    // =========================================================================

    @Test
    @DisplayName("execute updates minQuantity and returns saved stock when stock exists")
    void should_update_min_quantity_and_return_saved_stock() {
        // given
        Stock existingStock = buildExistingStock();
        when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(existingStock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Stock result = useCase.execute(STOCK_ID, NEW_MIN_QTY);

        // then
        assertThat(result.getMinQuantity()).isEqualByComparingTo(NEW_MIN_QTY);
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("execute passes the stock with updated minQuantity to repository.save")
    void should_pass_stock_with_correct_min_quantity_to_save() {
        // given
        Stock existingStock = buildExistingStock();
        when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(existingStock));

        ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
        when(stockRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(STOCK_ID, NEW_MIN_QTY);

        // then — verificamos el objeto exacto que llega al repositorio
        Stock captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(STOCK_ID);
        assertThat(captured.getMinQuantity()).isEqualByComparingTo(NEW_MIN_QTY);
        assertThat(captured.getUpdatedAt()).isNotNull();
    }

    // =========================================================================
    // GROUP 2 — Error de dominio: stock no existe => ResourceNotFoundException
    // =========================================================================

    @Test
    @DisplayName("execute throws ResourceNotFoundException when stock does not exist for given id")
    void should_throw_resource_not_found_when_stock_does_not_exist() {
        // given
        when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(STOCK_ID, NEW_MIN_QTY))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(STOCK_ID));
    }

    // =========================================================================
    // GROUP 3 — Cortocircuito: stock no existe => save nunca se invoca
    // =========================================================================

    @Test
    @DisplayName("execute never calls repository.save when stock does not exist")
    void should_never_call_save_when_stock_does_not_exist() {
        // given
        when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> useCase.execute(STOCK_ID, NEW_MIN_QTY))
                .isInstanceOf(ResourceNotFoundException.class);

        // then — cortocircuito: save no debe haberse llamado en ningún momento
        verify(stockRepository, never()).save(any());
    }
}
