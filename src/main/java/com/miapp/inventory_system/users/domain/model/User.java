package com.miapp.inventory_system.users.domain.model;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class User {

    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User() {}

    public static User create(
            String name,
            String email,
            String hashedPassword,
            Role role) {

        validate(name, email);

        User user = new User();
        user.name      = name;
        user.email     = email;
        user.password  = hashedPassword;
        user.role      = role;
        user.active    = true;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();

        return user;
    }

    public static User reconstitute(
            Long id,
            String name,
            String email,
            String password,
            Role role,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        User user = new User();
        user.id        = id;
        user.name      = name;
        user.email     = email;
        user.password  = password;
        user.role      = role;
        user.active    = active;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;

        return user;
    }

    public void update(
            String name,
            String email,
            Role role) {

        validate(name, email);

        this.name      = name;
        this.email     = email;
        this.role      = role;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalArgumentException(
                    "El usuario ya está desactivado");
        }
        this.active    = false;
        this.updatedAt = LocalDateTime.now();
    }

    private static void validate(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del usuario es obligatorio");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "El email del usuario es obligatorio");
        }
    }
}