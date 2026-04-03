package com.miapp.inventory_system.users.application.usecase;

import com.miapp.inventory_system.shared.exception.ResourceNotFoundException;
import com.miapp.inventory_system.users.application.command.ChangePasswordCommand;
import com.miapp.inventory_system.users.domain.model.User;
import com.miapp.inventory_system.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(ChangePasswordCommand command) {

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + command.userId()));

        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta");
        }

        String hashedNew = passwordEncoder.encode(command.newPassword());
        user.changePassword(hashedNew);

        userRepository.save(user);
    }
}
