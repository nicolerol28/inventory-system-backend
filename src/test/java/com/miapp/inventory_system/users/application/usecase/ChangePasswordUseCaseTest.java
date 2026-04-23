package com.miapp.inventory_system.users.application.usecase;

import com.miapp.inventory_system.shared.exception.ForbiddenException;
import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.users.application.command.ChangePasswordCommand;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangePasswordUseCase")
class ChangePasswordUseCaseTest {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final Long   USER_ID          = 9L;
    private static final String STORED_HASH      = "$2a$10$storedHash";
    private static final String CURRENT_PASSWORD = "OldPass1!";
    private static final String NEW_PASSWORD     = "NewPass2@";
    private static final String NEW_HASH         = "$2a$10$newHash";

    // -------------------------------------------------------------------------
    // Collaborators
    // -------------------------------------------------------------------------

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ChangePasswordUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User buildUser() {
        return User.reconstitute(USER_ID, "Luis Mora", "luis@example.com", STORED_HASH,
                null, Role.OPERATOR, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    // buildCommand: requester == owner (USER_ID), not admin — covers the own-password happy path
    private ChangePasswordCommand buildCommand() {
        return new ChangePasswordCommand(USER_ID, CURRENT_PASSWORD, NEW_PASSWORD, USER_ID, false);
    }

    // -------------------------------------------------------------------------
    // GROUP 1 — Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute changes password successfully when current password matches")
    void should_change_password_when_current_password_is_correct() {
        // given
        User user = buildUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_PASSWORD, STORED_HASH)).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_HASH);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        useCase.execute(buildCommand());

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo(NEW_HASH);
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
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(USER_ID));
    }

    // -------------------------------------------------------------------------
    // GROUP 3 — Error: current password does not match
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws IllegalArgumentException when current password is wrong")
    void should_throw_when_current_password_does_not_match() {
        // given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser()));
        when(passwordEncoder.matches(CURRENT_PASSWORD, STORED_HASH)).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Contraseña actual incorrecta");
    }

    // -------------------------------------------------------------------------
    // GROUP 4 — Short-circuit: user not found prevents encode and save
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute never checks password, encodes or saves when user not found")
    void should_not_check_password_or_save_when_user_not_found() {
        // given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        // then — short-circuit
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // GROUP 5 — Short-circuit: wrong current password prevents encode and save
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute never encodes new password or saves when current password is wrong")
    void should_not_encode_or_save_when_current_password_is_wrong() {
        // given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser()));
        when(passwordEncoder.matches(CURRENT_PASSWORD, STORED_HASH)).thenReturn(false);

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class);

        // then — short-circuit
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // GROUP 6 — IDOR: authorization checks
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute allows user to change their own password (requesterUserId == userId)")
    void should_allow_user_to_change_own_password() {
        // given 
        ChangePasswordCommand command = new ChangePasswordCommand(
                USER_ID, CURRENT_PASSWORD, NEW_PASSWORD, USER_ID, false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser()));
        when(passwordEncoder.matches(CURRENT_PASSWORD, STORED_HASH)).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_HASH);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when — must not throw
        useCase.execute(command);

        // then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("execute throws ForbiddenException when non-admin tries to change another user's password")
    void should_throw_forbidden_when_operator_tries_to_change_another_user_password() {
        // given 
        Long anotherRequesterId = 5L;
        ChangePasswordCommand command = new ChangePasswordCommand(
                USER_ID, CURRENT_PASSWORD, NEW_PASSWORD, anotherRequesterId, false);

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("permiso");

        // then — short-circuit: no DB access at all
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute allows admin to change another user's password")
    void should_allow_admin_to_change_another_user_password() {
        // given 
        Long adminId = 1L;
        Long targetUserId = 99L;
        User targetUser = User.reconstitute(targetUserId, "Other User", "other@example.com",
                STORED_HASH, null, Role.OPERATOR, true,
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        ChangePasswordCommand command = new ChangePasswordCommand(
                targetUserId, CURRENT_PASSWORD, NEW_PASSWORD, adminId, true);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(passwordEncoder.matches(CURRENT_PASSWORD, STORED_HASH)).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_HASH);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when — must not throw
        useCase.execute(command);

        // then
        verify(userRepository).save(any(User.class));
    }
}
