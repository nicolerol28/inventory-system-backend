package com.miapp.inventory_system.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    private static final String REQUEST_URI = "/api/test";

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
    }

    // -----------------------------------------------------------------------
    // GROUP 1 — handleIllegalArgument
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("handleIllegalArgument returns 400 Bad Request with correct body")
    void should_return_400_when_illegal_argument_exception_is_thrown() {
        // given
        IllegalArgumentException ex = new IllegalArgumentException("SKU already exists");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.error()).isEqualTo("Bad Request");
        assertThat(body.message()).isEqualTo("SKU already exists");
        assertThat(body.path()).isEqualTo(REQUEST_URI);
        assertThat(body.timestamp()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // GROUP 2 — handleInsufficientStock
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("handleInsufficientStock returns 422 Unprocessable Entity with correct body")
    void should_return_422_when_insufficient_stock_exception_is_thrown() {
        // given
        InsufficientStockException ex = new InsufficientStockException("Stock insuficiente: solo hay 5 unidades");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleInsufficientStock(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(422);
        assertThat(body.error()).isEqualTo("Unprocessable Entity");
        assertThat(body.message()).isEqualTo("Stock insuficiente: solo hay 5 unidades");
        assertThat(body.path()).isEqualTo(REQUEST_URI);
        assertThat(body.timestamp()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // GROUP 3 — handleResourceNotFound
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("handleResourceNotFound returns 404 Not Found with correct body")
    void should_return_404_when_resource_not_found_exception_is_thrown() {
        // given
        ResourceNotFoundException ex = new ResourceNotFoundException("Producto con id 99 no encontrado");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.error()).isEqualTo("Not Found");
        assertThat(body.message()).isEqualTo("Producto con id 99 no encontrado");
        assertThat(body.path()).isEqualTo(REQUEST_URI);
        assertThat(body.timestamp()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // GROUP 4 — handleGeneric
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("handleGeneric returns 500 with generic message, not the exception's message")
    void should_return_500_with_generic_message_when_unexpected_exception_is_thrown() {
        // given
        RuntimeException ex = new RuntimeException("Error inesperado en el servidor");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.error()).isEqualTo("Internal Server Error");
        assertThat(body.message()).isEqualTo("Error interno del servidor");
        assertThat(body.path()).isEqualTo(REQUEST_URI);
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleGeneric does not expose internal exception details to the client")
    void should_not_expose_internal_exception_message_in_500_response() {
        // given — an exception that contains sensitive internal details
        RuntimeException ex = new RuntimeException("TABLE_USERS does not exist in schema public — JDBC error");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex, request);

        // then — client receives a safe generic message, not the internal detail
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).doesNotContain("TABLE_USERS");
        assertThat(response.getBody().message()).doesNotContain("JDBC");
        assertThat(response.getBody().message()).isEqualTo("Error interno del servidor");
    }

    // -----------------------------------------------------------------------
    // GROUP 4b — handleForbidden
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("handleForbidden returns 403 Forbidden with correct body")
    void should_return_403_when_forbidden_exception_is_thrown() {
        // given
        ForbiddenException ex = new ForbiddenException("No tiene permiso para cambiar la contraseña de otro usuario");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleForbidden(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(403);
        assertThat(body.error()).isEqualTo("Forbidden");
        assertThat(body.message()).isEqualTo("No tiene permiso para cambiar la contraseña de otro usuario");
        assertThat(body.path()).isEqualTo(REQUEST_URI);
        assertThat(body.timestamp()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // GROUP 5 — handleValidation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("handleValidation returns 400 Validation Error with single field error message")
    void should_return_400_with_formatted_message_when_one_field_is_invalid() {
        // given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("productRequest", "name", "no puede estar vacío");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // when
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.error()).isEqualTo("Validation Error");
        assertThat(body.message()).isEqualTo("name: no puede estar vacío");
        assertThat(body.path()).isEqualTo(REQUEST_URI);
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleValidation concatenates multiple field errors with comma separator")
    void should_return_400_with_combined_message_when_multiple_fields_are_invalid() {
        // given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError nameError = new FieldError("productRequest", "name", "no puede estar vacío");
        FieldError skuError  = new FieldError("productRequest", "sku",  "formato inválido");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(nameError, skuError));

        // when
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.error()).isEqualTo("Validation Error");
        assertThat(body.message()).isEqualTo("name: no puede estar vacío, sku: formato inválido");
        assertThat(body.path()).isEqualTo(REQUEST_URI);
        assertThat(body.timestamp()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // GROUP 6 — handleAuthenticationException
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("handleAuthenticationException returns 401 Unauthorized with prefixed message")
    void should_return_401_when_authentication_exception_is_thrown() {
        // given
        AuthenticationException ex = mock(AuthenticationException.class);
        when(ex.getMessage()).thenReturn("Token expirado");

        // when
        ErrorResponse body = handler.handleAuthenticationException(ex, request);

        // then
        assertThat(body.status()).isEqualTo(401);
        assertThat(body.error()).isEqualTo("Unauthorized");
        assertThat(body.message()).startsWith("No autorizado: ");
        assertThat(body.message()).isEqualTo("No autorizado: Token expirado");
        assertThat(body.path()).isEqualTo(REQUEST_URI);
        assertThat(body.timestamp()).isNotNull();
    }
}
