package com.miapp.inventory_system.users.application.usecase;

import com.miapp.inventory_system.users.application.RegisterUserResult;
import com.miapp.inventory_system.users.application.command.RegisterUserCommand;
import com.miapp.inventory_system.users.domain.model.User;
import com.miapp.inventory_system.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterUserResult execute(RegisterUserCommand command) {

        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el email: " + command.email());
        }

        String hashedPassword = passwordEncoder.encode(command.password());

        User user = User.create(
                command.name(),
                command.email(),
                hashedPassword,
                command.role()
        );

        User saved = userRepository.save(user);
        return new RegisterUserResult(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole(), saved.isActive());
    }
}