package com.miapp.inventory_system.users.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User domain model")
class UserTest {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final String VALID_NAME     = "Ana García";
    private static final String VALID_EMAIL    = "ana@example.com";
    private static final String HASHED_PASS    = "$2a$10$hashedpassword";
    private static final Role   VALID_ROLE     = Role.OPERATOR;

    // -------------------------------------------------------------------------
    // GROUP 1 — create: happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create sets active=true, non-null timestamps, and correct field values")
    void should_set_active_true_and_timestamps_and_fields_on_create() {
        // given / when
        User user = User.create(VALID_NAME, VALID_EMAIL, HASHED_PASS, VALID_ROLE);

        // then
        assertThat(user.isActive()).isTrue();
        assertThat(user.getId()).isNull();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getName()).isEqualTo(VALID_NAME);
        assertThat(user.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(user.getPassword()).isEqualTo(HASHED_PASS);
        assertThat(user.getRole()).isEqualTo(VALID_ROLE);
        assertThat(user.getGoogleId()).isNull();
    }

    // -------------------------------------------------------------------------
    // GROUP 2 — create: invalid name
    // -------------------------------------------------------------------------

    static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of("   ")
        );
    }

    @ParameterizedTest(name = "[{index}] name=''{0}'' -> IllegalArgumentException")
    @MethodSource("invalidNames")
    @DisplayName("create throws IllegalArgumentException when name is null or blank")
    void should_throw_when_name_is_invalid_on_create(String invalidName) {
        assertThatThrownBy(() -> User.create(invalidName, VALID_EMAIL, HASHED_PASS, VALID_ROLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El nombre del usuario es obligatorio");
    }

    // -------------------------------------------------------------------------
    // GROUP 3 — create: invalid email
    // -------------------------------------------------------------------------

    static Stream<Arguments> invalidEmails() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of("   ")
        );
    }

    @ParameterizedTest(name = "[{index}] email=''{0}'' -> IllegalArgumentException")
    @MethodSource("invalidEmails")
    @DisplayName("create throws IllegalArgumentException when email is null or blank")
    void should_throw_when_email_is_invalid_on_create(String invalidEmail) {
        assertThatThrownBy(() -> User.create(VALID_NAME, invalidEmail, HASHED_PASS, VALID_ROLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El email del usuario es obligatorio");
    }

    // -------------------------------------------------------------------------
    // GROUP 4 — reconstitute: all fields preserved
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("reconstitute preserves all fields without validation, including active=false and googleId")
    void should_preserve_all_fields_on_reconstitute() {
        // given
        Long            id        = 42L;
        String          googleId  = "google-sub-123";
        boolean         active    = false;
        LocalDateTime   createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime   updatedAt = LocalDateTime.of(2024, 6, 20, 12, 0);

        // when
        User user = User.reconstitute(id, VALID_NAME, VALID_EMAIL, HASHED_PASS,
                googleId, Role.ADMIN, active, createdAt, updatedAt);

        // then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getName()).isEqualTo(VALID_NAME);
        assertThat(user.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(user.getPassword()).isEqualTo(HASHED_PASS);
        assertThat(user.getGoogleId()).isEqualTo(googleId);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.isActive()).isFalse();
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("reconstitute accepts null googleId without throwing")
    void should_accept_null_google_id_on_reconstitute() {
        // when
        User user = User.reconstitute(1L, VALID_NAME, VALID_EMAIL, HASHED_PASS,
                null, VALID_ROLE, true,
                LocalDateTime.now(), LocalDateTime.now());

        // then
        assertThat(user.getGoogleId()).isNull();
    }

    // -------------------------------------------------------------------------
    // GROUP 5 — update: happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("update changes name, email and role, and refreshes updatedAt")
    void should_update_name_email_role_and_refresh_updated_at() {
        // given
        User user = User.create(VALID_NAME, VALID_EMAIL, HASHED_PASS, VALID_ROLE);
        LocalDateTime beforeUpdate = user.getUpdatedAt();

        // when
        user.update("Nuevo Nombre", "nuevo@example.com", Role.ADMIN);

        // then
        assertThat(user.getName()).isEqualTo("Nuevo Nombre");
        assertThat(user.getEmail()).isEqualTo("nuevo@example.com");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    // -------------------------------------------------------------------------
    // GROUP 6 — update: invalid inputs
    // -------------------------------------------------------------------------

    static Stream<Arguments> invalidUpdateFields() {
        return Stream.of(
                Arguments.of(null,  VALID_EMAIL, "El nombre del usuario es obligatorio"),
                Arguments.of("   ", VALID_EMAIL, "El nombre del usuario es obligatorio"),
                Arguments.of(VALID_NAME, null,   "El email del usuario es obligatorio"),
                Arguments.of(VALID_NAME, "   ",  "El email del usuario es obligatorio")
        );
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("invalidUpdateFields")
    @DisplayName("update throws IllegalArgumentException for each invalid field")
    void should_throw_when_fields_are_invalid_on_update(
            String name, String email, String expectedMessage) {

        // given
        User user = User.create(VALID_NAME, VALID_EMAIL, HASHED_PASS, VALID_ROLE);

        // when / then
        assertThatThrownBy(() -> user.update(name, email, VALID_ROLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    // -------------------------------------------------------------------------
    // GROUP 7 — changePassword: happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("changePassword replaces password and refreshes updatedAt")
    void should_replace_password_and_refresh_updated_at_on_change_password() {
        // given
        User user = User.create(VALID_NAME, VALID_EMAIL, HASHED_PASS, VALID_ROLE);
        LocalDateTime beforeChange = user.getUpdatedAt();
        String newHashed = "$2a$10$newhashedpassword";

        // when
        user.changePassword(newHashed);

        // then
        assertThat(user.getPassword()).isEqualTo(newHashed);
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(beforeChange);
    }

    // -------------------------------------------------------------------------
    // GROUP 8 — linkGoogle: happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("linkGoogle assigns googleId and refreshes updatedAt")
    void should_assign_google_id_and_refresh_updated_at_on_link_google() {
        // given
        User user = User.create(VALID_NAME, VALID_EMAIL, HASHED_PASS, VALID_ROLE);
        LocalDateTime beforeLink = user.getUpdatedAt();
        String googleSub = "google-sub-xyz";

        // when
        user.linkGoogle(googleSub);

        // then
        assertThat(user.getGoogleId()).isEqualTo(googleSub);
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(beforeLink);
    }

    // -------------------------------------------------------------------------
    // GROUP 9 — deactivate: happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deactivate sets active=false when user is currently active")
    void should_set_active_false_on_deactivate() {
        // given
        User user = User.create(VALID_NAME, VALID_EMAIL, HASHED_PASS, VALID_ROLE);
        assertThat(user.isActive()).isTrue();

        // when
        user.deactivate();

        // then
        assertThat(user.isActive()).isFalse();
    }

    // -------------------------------------------------------------------------
    // GROUP 10 — deactivate: already inactive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deactivate throws IllegalArgumentException when user is already inactive")
    void should_throw_when_user_is_already_inactive_on_deactivate() {
        // given
        User user = User.reconstitute(1L, VALID_NAME, VALID_EMAIL, HASHED_PASS,
                null, VALID_ROLE, false, LocalDateTime.now(), LocalDateTime.now());

        // when / then
        assertThatThrownBy(user::deactivate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El usuario ya está desactivado");
    }
}
