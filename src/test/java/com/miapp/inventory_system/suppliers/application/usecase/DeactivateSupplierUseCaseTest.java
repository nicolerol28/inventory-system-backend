package com.miapp.inventory_system.suppliers.application.usecase;

import com.miapp.inventory_system.products.domain.repository.ProductRepository;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.suppliers.domain.model.Supplier;
import com.miapp.inventory_system.suppliers.domain.repository.SupplierRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateSupplierUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DeactivateSupplierUseCase useCase;

    private static final Long SUPPLIER_ID = 7L;

    /** Helper: returns a reconstituted active supplier. */
    private Supplier buildActiveSupplier() {
        LocalDateTime now = LocalDateTime.now();
        return Supplier.reconstitute(SUPPLIER_ID, "Proveedor Activo", "Contacto", "555111222",
                true, now, now);
    }

    // -------------------------------------------------------------------------
    // GROUP 1 — Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute deactivates supplier and saves when no active products are associated")
    void should_deactivate_supplier_successfully() {
        // given
        Supplier supplier = buildActiveSupplier();
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(supplier));
        when(productRepository.existsActiveBySupplierId(SUPPLIER_ID)).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(SUPPLIER_ID);

        // then — supplier must be saved as inactive
        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    // -------------------------------------------------------------------------
    // GROUP 2 — Domain errors
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws ResourceNotFoundException when supplier does not exist")
    void should_throw_when_supplier_not_found() {
        // given
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(SUPPLIER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(SUPPLIER_ID));
    }

    @Test
    @DisplayName("execute throws when supplier has active products associated")
    void should_throw_when_supplier_has_active_products() {
        // given
        Supplier supplier = buildActiveSupplier();
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(supplier));
        when(productRepository.existsActiveBySupplierId(SUPPLIER_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(SUPPLIER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productos activos");
    }

    // -------------------------------------------------------------------------
    // GROUP 3 — Short-circuit verification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute never checks active products or calls save when supplier is not found")
    void should_not_check_products_or_save_when_supplier_not_found() {
        // given
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> useCase.execute(SUPPLIER_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        // then — short-circuit: no further calls after failed lookup
        verify(productRepository, never()).existsActiveBySupplierId(any());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute never calls save when supplier has active products")
    void should_not_save_when_supplier_has_active_products() {
        // given
        Supplier supplier = buildActiveSupplier();
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(supplier));
        when(productRepository.existsActiveBySupplierId(SUPPLIER_ID)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(SUPPLIER_ID))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(supplierRepository, never()).save(any());
    }
}
