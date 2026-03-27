package com.miapp.inventory_system.users.infrastructure.security;

import com.miapp.inventory_system.users.infrastructure.entity.UserJpaEntity;
import com.miapp.inventory_system.users.infrastructure.repository.UserJpaRepositorySpring;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserJpaRepositorySpring jpaRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserJpaEntity entity = jpaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + email));

        if (!entity.isActive()) {
            throw new UsernameNotFoundException(
                    "Usuario inactivo: " + email);
        }

        return new org.springframework.security.core.userdetails.User(
                entity.getEmail(),
                entity.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + entity.getRole()))
        );
    }
}