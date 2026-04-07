package com.miapp.inventory_system.users.application.usecase;

import com.miapp.inventory_system.users.application.RegisterUserResult;
import com.miapp.inventory_system.users.application.command.RegisterUserCommand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserUseCase")
class RegisterUserUseCaseTest {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String USER_NAME    = "Carlos López";
    private static final String USER_EMAIL   = "carlos@example.com";
    private static final String RAW_PASSWORD = "S3cret!";
    private static final String HASHED_PASS  = "$2a$10$hashedCarlos";
    private static final Role   USER_ROLE    = Role.OPERATOR;

    // -------------------------------------------------------------------------
    // Collaborators
    // -------------------------------------------------------------------------

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private RegisterUserCommand buildCommand() {
        return new RegisterUserCommand(USER_NAME, USER_EMAIL, RAW_PASSWORD, USER_ROLE);
    }

    // -------------------------------------------------------------------------
    // GROUP 1 — Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute registers user successfully when email is not taken")
    void should_register_user_successfully_when_email_is_not_taken() {
        // given
        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASS);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        RegisterUserResult result = useCase.execute(buildCommand());

        // then
        assertThat(result.name()).isEqualTo(USER_NAME);
        assertThat(result.email()).isEqualTo(USER_EMAIL);
        assertThat(result.role()).isEqualTo(USER_ROLE);
        assertThat(result.active()).isTrue();

        // verify the saved user has the hashed password
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo(HASHED_PASS);
        assertThat(captor.getValue().isActive()).isTrue();
    }

    // -------------------------------------------------------------------------
    // GROUP 2 — Error: email already taken
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws IllegalArgumentException when email already exists")
    void should_throw_when_email_already_exists() {
        // given
        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(USER_EMAIL);
    }

    // -------------------------------------------------------------------------
    // GROUP 3 — Short-circuit: encode and save never called when email is taken
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute never encodes password or saves when email is already taken")
    void should_not_encode_or_save_when_email_already_exists() {
        // given
        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class);

        // then — short-circuit
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }
}
