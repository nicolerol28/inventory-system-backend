package com.miapp.inventory_system.users.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.shared.security.JwtService;
import com.miapp.inventory_system.users.application.LoginResult;
import com.miapp.inventory_system.users.domain.model.Role;
import com.miapp.inventory_system.users.domain.model.User;
import com.miapp.inventory_system.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleLoginUseCase")
class GoogleLoginUseCaseTest {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String CLIENT_ID   = "test-client-id";
    private static final String GOOGLE_SUB  = "google-sub-999";
    private static final String USER_EMAIL  = "user@test.com";
    private static final String USER_NAME   = "Test User";
    private static final Long   USER_ID     = 11L;
    private static final String ID_TOKEN    = "google-id-token-value";
    private static final String JWT_TOKEN   = "header.payload.signature";

    // -------------------------------------------------------------------------
    // Collaborators
    // -------------------------------------------------------------------------

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GoogleLoginUseCase useCase;

    // -------------------------------------------------------------------------
    // Setup — inject @Value field (technical debt workaround)
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "googleClientId", CLIENT_ID);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> buildValidPayload() {
        return Map.of("email", USER_EMAIL, "sub", GOOGLE_SUB, "aud", CLIENT_ID);
    }

    private User buildActiveUserWithoutGoogleId() {
        return User.reconstitute(USER_ID, USER_NAME, USER_EMAIL, "$2a$10$hashed",
                null, Role.OPERATOR, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private User buildActiveUserWithGoogleId() {
        return User.reconstitute(USER_ID, USER_NAME, USER_EMAIL, "$2a$10$hashed",
                GOOGLE_SUB, Role.OPERATOR, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private User buildInactiveUser() {
        return User.reconstitute(USER_ID, USER_NAME, USER_EMAIL, "$2a$10$hashed",
                null, Role.OPERATOR, false,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private void stubJwt() {
        when(jwtService.generateToken(USER_ID, USER_EMAIL, Role.OPERATOR.name(), USER_NAME))
                .thenReturn(JWT_TOKEN);
        when(jwtService.extractExpiresAt(JWT_TOKEN))
                .thenReturn(LocalDateTime.now().plusHours(1));
    }

    // -------------------------------------------------------------------------
    // GROUP 1 — Happy path: first Google login (googleId not yet linked)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute links googleId, saves user, and returns LoginResult on first Google login")
    void should_link_google_id_and_save_on_first_login() {
        // given
        User user = buildActiveUserWithoutGoogleId();
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(buildValidPayload());
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        stubJwt();

        // when
        LoginResult result = useCase.execute(ID_TOKEN);

        // then
        assertThat(result.token()).isEqualTo(JWT_TOKEN);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.role()).isEqualTo(Role.OPERATOR.name());

        // verify googleId was linked and user was persisted
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getGoogleId()).isEqualTo(GOOGLE_SUB);
    }

    // -------------------------------------------------------------------------
    // GROUP 2 — Happy path: repeated Google login (googleId already linked)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute does NOT call save for link when googleId is already set")
    void should_not_save_link_when_google_id_already_linked() {
        // given
        User user = buildActiveUserWithGoogleId();
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(buildValidPayload());
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        stubJwt();

        // when
        LoginResult result = useCase.execute(ID_TOKEN);

        // then
        assertThat(result.token()).isEqualTo(JWT_TOKEN);
        verify(userRepository, never()).save(any()); // no link-save call
        verify(jwtService).generateToken(USER_ID, USER_EMAIL, Role.OPERATOR.name(), USER_NAME);
    }

    // -------------------------------------------------------------------------
    // GROUP 3 — Error: RestTemplate throws exception (invalid token)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws IllegalArgumentException when RestTemplate throws during token verification")
    void should_throw_when_rest_template_throws() {
        // given
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RestClientException("connection error"));

        // when / then
        assertThatThrownBy(() -> useCase.execute(ID_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token de Google inválido");
    }

    // -------------------------------------------------------------------------
    // GROUP 4 — Error: payload is null
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws IllegalArgumentException when Google returns null payload")
    void should_throw_when_payload_is_null() {
        // given
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> useCase.execute(ID_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token de Google inválido");
    }

    // -------------------------------------------------------------------------
    // GROUP 5 — Error: aud does not match clientId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws IllegalArgumentException when aud in payload does not match clientId")
    void should_throw_when_aud_does_not_match_client_id() {
        // given
        Map<String, Object> wrongAudPayload = Map.of(
                "email", USER_EMAIL, "sub", GOOGLE_SUB, "aud", "wrong-client-id");
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(wrongAudPayload);

        // when / then
        assertThatThrownBy(() -> useCase.execute(ID_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token de Google inválido");
    }

    // -------------------------------------------------------------------------
    // GROUP 6 — Error: email not registered in the system
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws ResourceNotFoundException when email has no account in the system")
    void should_throw_when_email_not_found_in_system() {
        // given
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(buildValidPayload());
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> useCase.execute(ID_TOKEN))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No existe una cuenta");
    }

    // -------------------------------------------------------------------------
    // GROUP 7 — Error: account is deactivated
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute throws IllegalArgumentException when account is deactivated")
    void should_throw_when_account_is_deactivated() {
        // given
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(buildValidPayload());
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(buildInactiveUser()));

        // when / then
        assertThatThrownBy(() -> useCase.execute(ID_TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tu cuenta está desactivada.");
    }

    // -------------------------------------------------------------------------
    // GROUP 8 — Short-circuit: invalid token prevents repository call
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("execute never calls findByEmail when token verification fails")
    void should_not_call_repository_when_token_is_invalid() {
        // given
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RestClientException("timeout"));

        // when
        assertThatThrownBy(() -> useCase.execute(ID_TOKEN))
                .isInstanceOf(IllegalArgumentException.class);

        // then — short-circuit
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any(), anyString(), anyString(), anyString());
    }
}
