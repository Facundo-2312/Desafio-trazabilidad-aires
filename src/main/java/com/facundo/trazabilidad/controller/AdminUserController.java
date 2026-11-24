package com.facundo.trazabilidad.controller;

import com.facundo.trazabilidad.model.AppUser;
import com.facundo.trazabilidad.repository.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String users(Model model) {
        model.addAttribute("users", users.findAll());
        model.addAttribute("userForm", new AdminUserForm());
        return "admin-users";
    }

    @PostMapping
    public String createUser(@Valid @ModelAttribute("userForm") AdminUserForm form,
                             BindingResult result, Model model, RedirectAttributes redirect) {
        if (form.getPassword() != null
                && form.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            result.rejectValue("password", "password.tooLong",
                    "La contraseña no puede superar 72 bytes en UTF-8.");
        }
        String normalizedEmail = form.getEmail() == null ? ""
                : form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!normalizedEmail.isBlank() && users.existsByEmailIgnoreCase(normalizedEmail)) {
            result.rejectValue("email", "email.duplicate", "Ya existe una cuenta con ese correo.");
        }
        if (result.hasErrors()) {
            model.addAttribute("users", users.findAll());
            form.setPassword(null);
            return "admin-users";
        }

        users.save(new AppUser(normalizedEmail, passwordEncoder.encode(form.getPassword()), form.getRole()));
        redirect.addFlashAttribute("success", "La cuenta se creó correctamente.");
        return "redirect:/admin/users";
    }
}
