package com.miapp.inventory_system.suppliers.application.usecase;

import com.miapp.inventory_system.suppliers.application.command.RegisterSupplierCommand;
import com.miapp.inventory_system.suppliers.domain.model.Supplier;
import com.miapp.inventory_system.suppliers.domain.repository.SupplierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterSupplierUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private RegisterSupplierUseCase useCase;

    private static final String SUPPLIER_NAME    = "Proveedor S.A.";
    private static final String SUPPLIER_CONTACT = "Juan Pérez";
    private static final String SUPPLIER_PHONE   = "123456789";

    // -------------------------------------------------------------------------
    // GROUP 1 — Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute registers supplier successfully when phone is null")
    void should_register_supplier_without_phone() {
        // given
        RegisterSupplierCommand command =
                new RegisterSupplierCommand(SUPPLIER_NAME, SUPPLIER_CONTACT, null);
        when(supplierRepository.existsByName(SUPPLIER_NAME)).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Supplier result = useCase.execute(command);

        // then
        assertThat(result.getName()).isEqualTo(SUPPLIER_NAME);
        assertThat(result.getContact()).isEqualTo(SUPPLIER_CONTACT);
        assertThat(result.getPhone()).isNull();
        assertThat(result.isActive()).isTrue();
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    @DisplayName("execute registers supplier successfully when all fields including phone are provided")
    void should_register_supplier_with_phone() {
        // given
        RegisterSupplierCommand command =
                new RegisterSupplierCommand(SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);
        when(supplierRepository.existsByName(SUPPLIER_NAME)).thenReturn(false);
        when(supplierRepository.existsByPhone(SUPPLIER_PHONE)).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Supplier result = useCase.execute(command);

        // then
        assertThat(result.getName()).isEqualTo(SUPPLIER_NAME);
        assertThat(result.getPhone()).isEqualTo(SUPPLIER_PHONE);
        assertThat(result.isActive()).isTrue();
        verify(supplierRepository).existsByName(SUPPLIER_NAME);
        verify(supplierRepository).existsByPhone(SUPPLIER_PHONE);
        verify(supplierRepository).save(any(Supplier.class));
    }

    // -------------------------------------------------------------------------
    // GROUP 2 — Domain errors
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws when supplier name already exists")
    void should_throw_when_name_already_exists() {
        // given
        RegisterSupplierCommand command =
                new RegisterSupplierCommand(SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);
        when(supplierRepository.existsByName(SUPPLIER_NAME)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(SUPPLIER_NAME);
    }

    @Test
    @DisplayName("execute throws when supplier phone already exists")
    void should_throw_when_phone_already_exists() {
        // given
        RegisterSupplierCommand command =
                new RegisterSupplierCommand(SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);
        when(supplierRepository.existsByName(SUPPLIER_NAME)).thenReturn(false);
        when(supplierRepository.existsByPhone(SUPPLIER_PHONE)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(SUPPLIER_PHONE);
    }

    // -------------------------------------------------------------------------
    // GROUP 3 — Short-circuit verification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute never calls existsByPhone or save when name is already taken")
    void should_not_check_phone_or_save_when_name_already_exists() {
        // given
        RegisterSupplierCommand command =
                new RegisterSupplierCommand(SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);
        when(supplierRepository.existsByName(SUPPLIER_NAME)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        // then — short-circuit: phone check and save must not be reached
        verify(supplierRepository, never()).existsByPhone(any());
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute never calls existsByPhone when phone is null")
    void should_not_call_exists_by_phone_when_phone_is_null() {
        // given
        RegisterSupplierCommand command =
                new RegisterSupplierCommand(SUPPLIER_NAME, SUPPLIER_CONTACT, null);
        when(supplierRepository.existsByName(SUPPLIER_NAME)).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(command);

        // then — phone is null so the conditional block must be skipped entirely
        verify(supplierRepository, never()).existsByPhone(any());
    }

    @Test
    @DisplayName("execute never calls save when phone is already taken")
    void should_not_save_when_phone_already_exists() {
        // given
        RegisterSupplierCommand command =
                new RegisterSupplierCommand(SUPPLIER_NAME, SUPPLIER_CONTACT, SUPPLIER_PHONE);
        when(supplierRepository.existsByName(SUPPLIER_NAME)).thenReturn(false);
        when(supplierRepository.existsByPhone(SUPPLIER_PHONE)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(supplierRepository, never()).save(any());
    }
}
