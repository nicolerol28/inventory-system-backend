package com.miapp.inventory_system.users.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.users.domain.model.Role;
import com.miapp.inventory_system.users.domain.model.User;
import com.miapp.inventory_system.users.domain.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeactivateUserUseCase")
class DeactivateUserUseCaseTest {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final Long USER_ID = 5L;

    // -------------------------------------------------------------------------
    // Collaborators
    // -------------------------------------------------------------------------

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeactivateUserUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User buildActiveUser() {
        return User.reconstitute(USER_ID, "Marta Soto", "marta@example.com", "$2a$10$hashed",
                null, Role.OPERATOR, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private User buildInactiveUser() {
        return User.reconstitute(USER_ID, "Marta Soto", "marta@example.com", "$2a$10$hashed",
                null, Role.OPERATOR, false,
                LocalDateTime.now(), LocalDateTime.now());
    }

    // -------------------------------------------------------------------------
    // GROUP 1 — Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute deactivates active user and persists the change")
    void should_deactivate_active_user_and_save() {
        // given
        User activeUser = buildActiveUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(USER_ID);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    // -------------------------------------------------------------------------
    // GROUP 2 — Error: user not found
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws ResourceNotFoundException when user id does not exist")
    void should_throw_when_user_not_found() {
        // given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(USER_ID));

        verify(userRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // GROUP 3 — Error: user already inactive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws IllegalArgumentException when user is already inactive and never saves")
    void should_throw_when_user_already_inactive_and_not_save() {
        // given
        User inactiveUser = buildInactiveUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(inactiveUser));

        // when / then
        assertThatThrownBy(() -> useCase.execute(USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El usuario ya está desactivado");

        verify(userRepository, never()).save(any());
    }
}
