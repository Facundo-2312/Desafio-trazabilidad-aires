package com.facundo.trazabilidad.controller;

import com.facundo.trazabilidad.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminUserForm {
    @NotBlank
    @Email
    @Size(max = 254)
    private String email;

    @NotBlank
    @Size(min = 12, max = 72)
    private String password;

    @NotNull
    private UserRole role = UserRole.USER;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}
