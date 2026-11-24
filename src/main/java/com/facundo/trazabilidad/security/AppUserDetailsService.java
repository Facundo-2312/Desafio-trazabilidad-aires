package com.facundo.trazabilidad.security;

import com.facundo.trazabilidad.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository users;

    public AppUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var account = users.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Cuenta no encontrada"));
        return User.withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .roles(account.getRole().name())
                .build();
    }
}
