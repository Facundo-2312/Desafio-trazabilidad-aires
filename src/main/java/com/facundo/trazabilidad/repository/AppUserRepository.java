package com.facundo.trazabilidad.repository;

import com.facundo.trazabilidad.model.AppUser;
import com.facundo.trazabilidad.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByRole(UserRole role);
}
