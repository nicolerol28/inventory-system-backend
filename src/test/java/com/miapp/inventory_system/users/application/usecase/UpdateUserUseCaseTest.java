package com.miapp.inventory_system.users.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.shared.security.JwtService;
import com.miapp.inventory_system.users.application.UpdateUserResult;
import com.miapp.inventory_system.users.application.command.UpdateUserCommand;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserUseCase")
class UpdateUserUseCaseTest {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final Long   USER_ID      = 3L;
    private static final String ORIGINAL_NAME  = "Pedro Torres";
    private static final String ORIGINAL_EMAIL = "pedro@example.com";
    private static final String NEW_NAME      = "Pedro A. Torres";
    private static final String NEW_EMAIL     = "pedro.new@example.com";
    private static final Role   NEW_ROLE      = Role.ADMIN;
    private static final String JWT_TOKEN     = "header.payload.signature";

    // -------------------------------------------------------------------------
    // Collaborators
    // -------------------------------------------------------------------------

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UpdateUserUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User buildExistingUser() {
        return User.reconstitute(USER_ID, ORIGINAL_NAME, ORIGINAL_EMAIL, "$2a$10$hashed",
                null, Role.OPERATOR, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private UpdateUserCommand buildCommand() {
        return new UpdateUserCommand(USER_ID, NEW_NAME, NEW_EMAIL, NEW_ROLE);
    }

    // -------------------------------------------------------------------------
    // GROUP 1 — Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute updates user and returns UpdateUserResult with new token")
    void should_update_user_and_return_result_when_data_is_valid() {
        // given
        User existing = buildExistingUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot(NEW_EMAIL, USER_ID)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(USER_ID, NEW_EMAIL, NEW_ROLE.name(), NEW_NAME))
                .thenReturn(JWT_TOKEN);

        // when
        UpdateUserResult result = useCase.execute(buildCommand());

        // then
        assertThat(result.token()).isEqualTo(JWT_TOKEN);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.name()).isEqualTo(NEW_NAME);
        assertThat(result.email()).isEqualTo(NEW_EMAIL);
        assertThat(result.role()).isEqualTo(NEW_ROLE);
        assertThat(result.active()).isTrue();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(NEW_NAME);
        assertThat(captor.getValue().getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(captor.getValue().getRole()).isEqualTo(NEW_ROLE);
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
    // GROUP 3 — Error: email already taken by another user
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws IllegalArgumentException when email is already taken by another user")
    void should_throw_when_email_already_taken_by_another_user() {
        // given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildExistingUser()));
        when(userRepository.existsByEmailAndIdNot(NEW_EMAIL, USER_ID)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NEW_EMAIL);
    }

    // -------------------------------------------------------------------------
    // GROUP 4 — Short-circuit: user not found prevents email check, save and JWT
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute never checks email, saves or generates token when user not found")
    void should_not_check_email_or_save_when_user_not_found() {
        // given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        // then — short-circuit
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any(), anyString(), anyString(), anyString());
    }

    // -------------------------------------------------------------------------
    // GROUP 5 — Short-circuit: email taken prevents save and JWT generation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute never saves or generates token when email is already taken")
    void should_not_save_or_generate_token_when_email_already_taken() {
        // given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildExistingUser()));
        when(userRepository.existsByEmailAndIdNot(NEW_EMAIL, USER_ID)).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
                .isInstanceOf(IllegalArgumentException.class);

        // then — short-circuit
        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any(), anyString(), anyString(), anyString());
    }
}
