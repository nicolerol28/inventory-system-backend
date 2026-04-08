package com.miapp.inventory_system.assistant.application.usecase;

import com.miapp.inventory_system.assistant.application.command.ChatCommand;
import com.miapp.inventory_system.assistant.application.port.GeminiGateway;
import com.miapp.inventory_system.assistant.application.query.InventoryContextQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendChatMessageUseCase — unit tests")
class SendChatMessageUseCaseTest {

    @Mock
    private InventoryContextQueryService inventoryContextQueryService;

    @Mock
    private GeminiGateway geminiGateway;

    @InjectMocks
    private SendChatMessageUseCase useCase;

    private static final String USER_MESSAGE  = "How many active products are there?";
    private static final String CLIENT_IP     = "192.168.1.10";
    private static final String FAKE_CONTEXT  = "- Total productos activos: 42";
    private static final String GEMINI_REPLY  = "There are 42 active products in the system.";

    // ─────────────────────────────────────────────────────────────────────────
    // GROUP 1 — Happy path
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("execute returns the reply from GeminiGateway when context and message are valid")
    void should_return_gemini_reply_when_context_and_message_are_valid() {
        // given
        ChatCommand command = new ChatCommand(USER_MESSAGE, CLIENT_IP);
        when(inventoryContextQueryService.getContext()).thenReturn(FAKE_CONTEXT);
        when(geminiGateway.send(anyString())).thenReturn(GEMINI_REPLY);

        // when
        String result = useCase.execute(command);

        // then
        assertThat(result).isEqualTo(GEMINI_REPLY);
        verify(inventoryContextQueryService).getContext();
        verify(geminiGateway).send(anyString());
    }

    @Test
    @DisplayName("execute builds a prompt that contains both the inventory context and the user message")
    void should_build_prompt_containing_context_and_user_message() {
        // given
        ChatCommand command = new ChatCommand(USER_MESSAGE, CLIENT_IP);
        when(inventoryContextQueryService.getContext()).thenReturn(FAKE_CONTEXT);
        when(geminiGateway.send(anyString())).thenReturn(GEMINI_REPLY);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        // when
        useCase.execute(command);

        // then
        verify(geminiGateway).send(promptCaptor.capture());
        String capturedPrompt = promptCaptor.getValue();
        assertThat(capturedPrompt).contains(FAKE_CONTEXT);
        assertThat(capturedPrompt).contains(USER_MESSAGE);
    }

    @Test
    @DisplayName("execute includes the system instructions in the prompt sent to Gemini")
    void should_include_system_instructions_in_prompt() {
        // given
        ChatCommand command = new ChatCommand(USER_MESSAGE, CLIENT_IP);
        when(inventoryContextQueryService.getContext()).thenReturn(FAKE_CONTEXT);
        when(geminiGateway.send(anyString())).thenReturn(GEMINI_REPLY);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        // when
        useCase.execute(command);

        // then — verify that the static system instructions are present
        verify(geminiGateway).send(promptCaptor.capture());
        String capturedPrompt = promptCaptor.getValue();
        assertThat(capturedPrompt).contains("asistente inteligente de gestión de inventario");
        assertThat(capturedPrompt).contains("Responde SIEMPRE en español");
    }

    @Test
    @DisplayName("execute appends user message at the end of the full prompt")
    void should_append_user_message_after_context_in_prompt() {
        // given
        ChatCommand command = new ChatCommand(USER_MESSAGE, CLIENT_IP);
        when(inventoryContextQueryService.getContext()).thenReturn(FAKE_CONTEXT);
        when(geminiGateway.send(anyString())).thenReturn(GEMINI_REPLY);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        // when
        useCase.execute(command);

        // then — context must appear before the user message in the final prompt
        verify(geminiGateway).send(promptCaptor.capture());
        String capturedPrompt = promptCaptor.getValue();
        int contextIndex     = capturedPrompt.indexOf(FAKE_CONTEXT);
        int userMessageIndex = capturedPrompt.indexOf(USER_MESSAGE);
        assertThat(contextIndex).isLessThan(userMessageIndex);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GROUP 2 — Error handling (GeminiGateway failure)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("execute propagates RuntimeException thrown by GeminiGateway")
    void should_propagate_exception_when_gemini_gateway_fails() {
        // given
        ChatCommand command = new ChatCommand(USER_MESSAGE, CLIENT_IP);
        when(inventoryContextQueryService.getContext()).thenReturn(FAKE_CONTEXT);
        when(geminiGateway.send(anyString()))
                .thenThrow(new RuntimeException("Gemini service unavailable"));

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Gemini service unavailable");
    }

    @Test
    @DisplayName("execute propagates RuntimeException thrown by InventoryContextQueryService")
    void should_propagate_exception_when_context_service_fails() {
        // given
        ChatCommand command = new ChatCommand(USER_MESSAGE, CLIENT_IP);
        when(inventoryContextQueryService.getContext())
                .thenThrow(new RuntimeException("Database unavailable"));

        // when / then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database unavailable");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GROUP 3 — Short-circuit: GeminiGateway is never called when context fails
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("execute never calls GeminiGateway when InventoryContextQueryService throws")
    void should_never_call_gemini_when_context_service_throws() {
        // given
        ChatCommand command = new ChatCommand(USER_MESSAGE, CLIENT_IP);
        when(inventoryContextQueryService.getContext())
                .thenThrow(new RuntimeException("Database unavailable"));

        // when
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(RuntimeException.class);

        // then — GeminiGateway must not be invoked at all
        verify(geminiGateway, never()).send(anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GROUP 4 — Interaction verification
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("execute calls InventoryContextQueryService exactly once per invocation")
    void should_call_context_service_exactly_once() {
        // given
        ChatCommand command = new ChatCommand(USER_MESSAGE, CLIENT_IP);
        when(inventoryContextQueryService.getContext()).thenReturn(FAKE_CONTEXT);
        when(geminiGateway.send(anyString())).thenReturn(GEMINI_REPLY);

        // when
        useCase.execute(command);

        // then
        verify(inventoryContextQueryService, times(1)).getContext();
    }

    @Test
    @DisplayName("execute calls GeminiGateway exactly once per invocation")
    void should_call_gemini_gateway_exactly_once() {
        // given
        ChatCommand command = new ChatCommand(USER_MESSAGE, CLIENT_IP);
        when(inventoryContextQueryService.getContext()).thenReturn(FAKE_CONTEXT);
        when(geminiGateway.send(anyString())).thenReturn(GEMINI_REPLY);

        // when
        useCase.execute(command);

        // then
        verify(geminiGateway, times(1)).send(anyString());
    }

    @Test
    @DisplayName("execute works correctly with an empty inventory context string")
    void should_handle_empty_context_without_error() {
        // given
        ChatCommand command = new ChatCommand(USER_MESSAGE, CLIENT_IP);
        when(inventoryContextQueryService.getContext()).thenReturn("");
        when(geminiGateway.send(anyString())).thenReturn(GEMINI_REPLY);

        // when
        String result = useCase.execute(command);

        // then
        assertThat(result).isEqualTo(GEMINI_REPLY);
        verify(geminiGateway).send(anyString());
    }
}
