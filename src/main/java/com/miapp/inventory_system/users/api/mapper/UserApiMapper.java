package com.miapp.inventory_system.users.api.mapper;

import com.miapp.inventory_system.users.api.dto.ChangePasswordRequest;
import com.miapp.inventory_system.users.api.dto.RegisterUserRequest;
import com.miapp.inventory_system.users.api.dto.UpdateUserRequest;
import com.miapp.inventory_system.users.api.dto.UserResponse;
import com.miapp.inventory_system.users.application.command.ChangePasswordCommand;
import com.miapp.inventory_system.users.application.command.RegisterUserCommand;
import com.miapp.inventory_system.users.application.command.UpdateUserCommand;
import com.miapp.inventory_system.users.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserApiMapper {

    public RegisterUserCommand toCommand(RegisterUserRequest request) {
        return new RegisterUserCommand(
                request.name(),
                request.email(),
                request.password(),
                request.role()
        );
    }

    public UpdateUserCommand toCommand(UpdateUserRequest request, Long id) {
        return new UpdateUserCommand(
                id,
                request.name(),
                request.email(),
                request.role()
        );
    }

    public ChangePasswordCommand toCommand(ChangePasswordRequest request, Long id,
                                          Long requesterUserId, boolean requesterIsAdmin) {
        return new ChangePasswordCommand(
                id, request.currentPassword(), request.newPassword(),
                requesterUserId, requesterIsAdmin);
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}