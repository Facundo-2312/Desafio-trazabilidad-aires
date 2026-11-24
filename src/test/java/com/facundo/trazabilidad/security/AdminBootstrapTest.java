package com.facundo.trazabilidad.security;

import com.facundo.trazabilidad.model.UserRole;
import com.facundo.trazabilidad.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminBootstrapTest {
    @Test
    void refusesToStartWithoutAnExistingAdminOrExplicitBootstrapCredentials() {
        AppUserRepository users = mock(AppUserRepository.class);
        AdminBootstrap bootstrap = new AdminBootstrap(users, new BCryptPasswordEncoder(), "", "");

        assertThrows(IllegalStateException.class,
                () -> bootstrap.run(new DefaultApplicationArguments(new String[0])));
    }

    @Test
    void refusesPartialBootstrapConfiguration() {
        AppUserRepository users = mock(AppUserRepository.class);
        AdminBootstrap bootstrap = new AdminBootstrap(users, new BCryptPasswordEncoder(),
                "admin@example.test", "");

        assertThrows(IllegalStateException.class,
                () -> bootstrap.run(new DefaultApplicationArguments(new String[0])));
        verify(users, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void existingAdminMakesBootstrapCredentialsOptional() throws Exception {
        AppUserRepository users = mock(AppUserRepository.class);
        when(users.existsByRole(UserRole.ADMIN)).thenReturn(true);
        AdminBootstrap bootstrap = new AdminBootstrap(users, new BCryptPasswordEncoder(), "", "");

        bootstrap.run(new DefaultApplicationArguments(new String[0]));

        verify(users, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
