package com.miapp.inventory_system.users.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.shared.security.JwtService;
import com.miapp.inventory_system.users.application.LoginResult;
import com.miapp.inventory_system.users.domain.model.User;
import com.miapp.inventory_system.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleLoginUseCase {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    private static final String GOOGLE_TOKEN_INFO_URL =
            "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @Transactional
    public LoginResult execute(String idToken) {
        Map<String, Object> payload = verifyToken(idToken);

        String email = (String) payload.get("email");
        String sub   = (String) payload.get("sub");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una cuenta para este correo. Contacta al administrador."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Tu cuenta está desactivada.");
        }

        if (user.getGoogleId() == null) {
            user.linkGoogle(sub);
            userRepository.save(user);
        }

        String token     = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name(), user.getName());
        return new LoginResult(token, user.getId(), user.getRole().name(), jwtService.extractExpiresAt(token));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> verifyToken(String idToken) {
        Map<String, Object> payload;

        try {
            payload = restTemplate.getForObject(GOOGLE_TOKEN_INFO_URL + idToken, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Token de Google inválido");
        }

        if (payload == null) {
            throw new IllegalArgumentException("Token de Google inválido");
        }

        String aud = (String) payload.get("aud");
        if (!googleClientId.equals(aud)) {
            throw new IllegalArgumentException("Token de Google inválido");
        }

        return payload;
    }
}
