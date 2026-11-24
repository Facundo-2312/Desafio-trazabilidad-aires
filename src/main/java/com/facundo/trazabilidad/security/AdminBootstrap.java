package com.facundo.trazabilidad.security;

import com.facundo.trazabilidad.model.AppUser;
import com.facundo.trazabilidad.model.UserRole;
import com.facundo.trazabilidad.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminBootstrap implements ApplicationRunner {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(AppUserRepository users, PasswordEncoder passwordEncoder,
                          @Value("${app.auth.bootstrap-admin.email:}") String adminEmail,
                          @Value("${app.auth.bootstrap-admin.password:}") String adminPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim();
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean hasEmail = !adminEmail.isBlank();
        boolean hasPassword = !adminPassword.isBlank();
        if (hasEmail != hasPassword) {
            throw new IllegalStateException("Configure both BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD.");
        }
        if (users.existsByRole(UserRole.ADMIN)) {
            return;
        }
        if (!hasEmail) {
            throw new IllegalStateException(
                    "No administrator exists. Configure BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD to bootstrap one.");
        }
        if (!adminEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL must be a valid email address.");
        }
        if (adminPassword.length() < 12 || adminPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_PASSWORD must contain at least 12 characters and no more than 72 UTF-8 bytes.");
        }
        if (users.existsByEmailIgnoreCase(adminEmail)) {
            throw new IllegalStateException("The bootstrap email already belongs to a non-administrator account.");
        }

        users.save(new AppUser(adminEmail, passwordEncoder.encode(adminPassword), UserRole.ADMIN));
    }
}
