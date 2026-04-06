package com.miapp.inventory_system.users.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.shared.security.JwtService;
import com.miapp.inventory_system.users.application.UpdateUserResult;
import com.miapp.inventory_system.users.application.command.UpdateUserCommand;
import com.miapp.inventory_system.users.domain.model.User;
import com.miapp.inventory_system.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public UpdateUserResult execute(UpdateUserCommand command) {

        User user = userRepository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + command.id()));

        if (userRepository.existsByEmailAndIdNot(command.email(), command.id())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el email: " + command.email());
        }

        user.update(command.name(), command.email(), command.role());

        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name(), user.getName());
        return new UpdateUserResult(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive());
    }
}