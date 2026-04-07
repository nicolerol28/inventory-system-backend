package com.miapp.inventory_system.users.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.shared.security.JwtService;
import com.miapp.inventory_system.users.application.LoginResult;
import com.miapp.inventory_system.users.domain.model.Role;
import com.miapp.inventory_system.users.domain.model.User;
import com.miapp.inventory_system.users.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase")
class LoginUseCaseTest {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String EMAIL       = "laura@example.com";
    private static final String PASSWORD    = "P@ssw0rd";
    private static final String JWT_TOKEN   = "header.payload.signature";
    private static final Long   USER_ID     = 7L;
    private static final String USER_NAME   = "Laura Ruiz";

    // -------------------------------------------------------------------------
    // Collaborators
    // -------------------------------------------------------------------------

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LoginUseCase useCase;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User buildUser() {
        return User.reconstitute(USER_ID, USER_NAME, EMAIL, "$2a$10$hashed",
                null, Role.ADMIN, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    // -------------------------------------------------------------------------
    // GROUP 1 — Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute returns LoginResult with token when credentials are valid")
    void should_return_login_result_when_credentials_are_valid() {
        // given
        User user = buildUser();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // authenticate returns Authentication, null means success in mocked context
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(USER_ID, EMAIL, Role.ADMIN.name(), USER_NAME))
                .thenReturn(JWT_TOKEN);
        when(jwtService.extractExpiresAt(JWT_TOKEN)).thenReturn(expiresAt);

        // when
        LoginResult result = useCase.execute(EMAIL, PASSWORD);

        // then
        assertThat(result.token()).isEqualTo(JWT_TOKEN);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.role()).isEqualTo(Role.ADMIN.name());
        assertThat(result.expiresAt()).isEqualTo(expiresAt);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(EMAIL);
        verify(jwtService).generateToken(USER_ID, EMAIL, Role.ADMIN.name(), USER_NAME);
    }

    // -------------------------------------------------------------------------
    // GROUP 2 — Error: user not found after successful authentication
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws ResourceNotFoundException when user is not found after authentication")
    void should_throw_when_user_not_found_after_authentication() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(EMAIL, PASSWORD))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(EMAIL);
    }

    // -------------------------------------------------------------------------
    // GROUP 3 — Short-circuit: auth failure prevents repository and JWT calls
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute never calls findByEmail or generateToken when authentication fails")
    void should_not_call_repository_or_jwt_when_authentication_fails() {
        // given — Spring Security throws when credentials are wrong
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // when
        assertThatThrownBy(() -> useCase.execute(EMAIL, "wrong-pass"))
                .isInstanceOf(BadCredentialsException.class);

        // then — short-circuit
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any(), anyString(), anyString(), anyString());
    }
}
