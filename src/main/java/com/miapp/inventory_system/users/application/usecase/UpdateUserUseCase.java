package com.miapp.inventory_system.users.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
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

    @Transactional
    public User execute(UpdateUserCommand command) {

        User user = userRepository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + command.id()));

        if (userRepository.existsByEmailAndIdNot(command.email(), command.id())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el email: " + command.email());
        }

        user.update(command.name(), command.email(), command.role());

        return userRepository.save(user);
    }
}