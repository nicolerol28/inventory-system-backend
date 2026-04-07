package com.miapp.inventory_system.suppliers.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.suppliers.application.command.UpdateSupplierCommand;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSupplierUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private UpdateSupplierUseCase useCase;

    private static final Long   SUPPLIER_ID      = 10L;
    private static final String SUPPLIER_NAME    = "Proveedor Actualizado";
    private static final String SUPPLIER_CONTACT = "María López";
    private static final String SUPPLIER_PHONE   = "999888777";

    /** Helper: builds a reconstituted active supplier with the test ID. */
    private Supplier buildExistingSupplier() {
        LocalDateTime now = LocalDateTime.now();
        return Supplier.reconstitute(SUPPLIER_ID, "Proveedor Original", "Contacto Original",
                "111000111", true, now, now);
    }

    // -------------------------------------------------------------------------
    // GROUP 1 — Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute updates supplier fields successfully when name and phone are unique")
    void should_update_supplier_successfully() {
        // given
        Supplier existing = buildExistingSupplier();
        UpdateSupplierCommand command =
                new UpdateSupplierCommand(SUPPLIER_ID, SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);

        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(existing));
        when(supplierRepository.existsByNameAndIdNot(SUPPLIER_NAME, SUPPLIER_ID)).thenReturn(false);
        when(supplierRepository.existsByPhoneAndIdNot(SUPPLIER_PHONE, SUPPLIER_ID)).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Supplier result = useCase.execute(command);

        // then
        assertThat(result.getName()).isEqualTo(SUPPLIER_NAME);
        assertThat(result.getContact()).isEqualTo(SUPPLIER_CONTACT);
        assertThat(result.getPhone()).isEqualTo(SUPPLIER_PHONE);

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(SUPPLIER_NAME);
    }

    @Test
    @DisplayName("execute updates supplier successfully and never calls existsByPhoneAndIdNot when phone is null")
    void should_update_supplier_without_phone_check_when_phone_is_null() {
        // given
        Supplier existing = buildExistingSupplier();
        UpdateSupplierCommand command =
                new UpdateSupplierCommand(SUPPLIER_ID, SUPPLIER_NAME, SUPPLIER_CONTACT, null);

        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(existing));
        when(supplierRepository.existsByNameAndIdNot(SUPPLIER_NAME, SUPPLIER_ID)).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(command);

        // then — phone is null so the phone uniqueness check must be skipped
        verify(supplierRepository, never()).existsByPhoneAndIdNot(any(), anyLong());
        verify(supplierRepository).save(any(Supplier.class));
    }

    // -------------------------------------------------------------------------
    // GROUP 2 — Domain errors
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws ResourceNotFoundException when supplier is not found")
    void should_throw_when_supplier_not_found() {
        // given
        UpdateSupplierCommand command =
                new UpdateSupplierCommand(SUPPLIER_ID, SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(SUPPLIER_ID));
    }

    @Test
    @DisplayName("execute throws when another supplier already has the same name")
    void should_throw_when_name_is_taken_by_another_supplier() {
        // given
        Supplier existing = buildExistingSupplier();
        UpdateSupplierCommand command =
                new UpdateSupplierCommand(SUPPLIER_ID, SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);

        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(existing));
        when(supplierRepository.existsByNameAndIdNot(SUPPLIER_NAME, SUPPLIER_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(SUPPLIER_NAME);
    }

    @Test
    @DisplayName("execute throws when another supplier already has the same phone")
    void should_throw_when_phone_is_taken_by_another_supplier() {
        // given
        Supplier existing = buildExistingSupplier();
        UpdateSupplierCommand command =
                new UpdateSupplierCommand(SUPPLIER_ID, SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);

        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(existing));
        when(supplierRepository.existsByNameAndIdNot(SUPPLIER_NAME, SUPPLIER_ID)).thenReturn(false);
        when(supplierRepository.existsByPhoneAndIdNot(SUPPLIER_PHONE, SUPPLIER_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(SUPPLIER_PHONE);
    }

    // -------------------------------------------------------------------------
    // GROUP 3 — Short-circuit verification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute never calls existsByNameAndIdNot or save when supplier is not found")
    void should_not_check_uniqueness_or_save_when_supplier_not_found() {
        // given
        UpdateSupplierCommand command =
                new UpdateSupplierCommand(SUPPLIER_ID, SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class);

        // then — short-circuit: no further calls after failed lookup
        verify(supplierRepository, never()).existsByNameAndIdNot(anyString(), anyLong());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute never calls save when name is already taken by another supplier")
    void should_not_save_when_name_is_taken() {
        // given
        Supplier existing = buildExistingSupplier();
        UpdateSupplierCommand command =
                new UpdateSupplierCommand(SUPPLIER_ID, SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);

        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(existing));
        when(supplierRepository.existsByNameAndIdNot(SUPPLIER_NAME, SUPPLIER_ID)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute never calls save when phone is already taken by another supplier")
    void should_not_save_when_phone_is_taken() {
        // given
        Supplier existing = buildExistingSupplier();
        UpdateSupplierCommand command =
                new UpdateSupplierCommand(SUPPLIER_ID, SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);

        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(existing));
        when(supplierRepository.existsByNameAndIdNot(SUPPLIER_NAME, SUPPLIER_ID)).thenReturn(false);
        when(supplierRepository.existsByPhoneAndIdNot(SUPPLIER_PHONE, SUPPLIER_ID)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(supplierRepository, never()).save(any());
    }
}
