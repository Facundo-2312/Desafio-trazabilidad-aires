package com.facundo.trazabilidad;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import com.facundo.trazabilidad.model.AppUser;
import com.facundo.trazabilidad.model.AirConditioner;
import com.facundo.trazabilidad.model.UserRole;
import com.facundo.trazabilidad.repository.AppUserRepository;
import com.facundo.trazabilidad.repository.AirConditionerRepository;
import com.facundo.trazabilidad.repository.ServiceRecordRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth-tests;DB_CLOSE_DELAY=-1",
        "app.example-data.enabled=false",
        "app.auth.bootstrap-admin.email=bootstrap-test@example.test",
        "app.auth.bootstrap-admin.password=Test-Only-Admin-Password-478!"
})
@AutoConfigureMockMvc
class TrazabilidadAiresApplicationTests {
    private static final String BOOTSTRAP_PASSWORD = "Test-Only-Admin-Password-478!";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AppUserRepository users;
    @Autowired
    private AirConditionerRepository equipment;
    @Autowired
    private ServiceRecordRepository services;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    @Test
    void homeRedirectsToEquipmentDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));
    }

    @Test
    void protectedPagesRequireAuthenticationAndLoginIsAvailable() throws Exception {
        mockMvc.perform(get("/equipos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("_csrf")));
    }

    @Test
    void bootstrapCreatesOnlyAHashedAdminCredential() {
        AppUser admin = users.findByEmailIgnoreCase("bootstrap-test@example.test").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(UserRole.ADMIN, admin.getRole());
        org.junit.jupiter.api.Assertions.assertNotEquals(BOOTSTRAP_PASSWORD, admin.getPasswordHash());
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches(BOOTSTRAP_PASSWORD, admin.getPasswordHash()));
    }

    @Test
    void configuredAdminCanAuthenticateWithItsPassword() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "bootstrap-test@example.test")
                        .param("password", BOOTSTRAP_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));
    }

    @Test
    void allAuthenticatedUsersShareInventoryWhileOnlyAdminsManageAccounts() throws Exception {
        users.save(new AppUser("alice@example.test",
                passwordEncoder.encode("Alice-password-123!"), UserRole.USER));
        users.save(new AppUser("bob@example.test",
                passwordEncoder.encode("Bob-password-12345!"), UserRole.USER));
        AirConditioner sharedUnit = equipment.save(new AirConditioner(
                "Shared company unit", "", "", "", "", "", null, "", null, ""));

        mockMvc.perform(get("/equipos").with(user("alice@example.test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Shared company unit")));
        mockMvc.perform(get("/equipos/" + sharedUnit.getId()).with(user("bob@example.test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Shared company unit")));
        mockMvc.perform(get("/equipos").with(user("bob@example.test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Shared company unit")));
        mockMvc.perform(get("/equipos").with(user("bootstrap-test@example.test").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Shared company unit")));
        mockMvc.perform(get("/admin/users").with(user("alice@example.test").roles("USER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/data").with(user("alice@example.test").roles("USER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/users").with(user("bootstrap-test@example.test").roles("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/data").with(user("bootstrap-test@example.test").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void csrfProtectsWritesAndAdminCreatesIndividuallyHashedAccounts() throws Exception {
        users.save(new AppUser("csrf-user@example.test",
                passwordEncoder.encode("Csrf-user-password-123!"), UserRole.USER));
        mockMvc.perform(post("/equipos")
                        .with(user("csrf-user@example.test").roles("USER"))
                        .param("name", "Without token"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/users")
                        .with(user("bootstrap-test@example.test").roles("ADMIN"))
                        .with(csrf())
                        .param("email", "created-user@example.test")
                        .param("password", "Created-user-password-123!")
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        AppUser created = users.findByEmailIgnoreCase("created-user@example.test").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(UserRole.USER, created.getRole());
        org.junit.jupiter.api.Assertions.assertNotEquals("Created-user-password-123!", created.getPasswordHash());
        org.junit.jupiter.api.Assertions.assertTrue(
                passwordEncoder.matches("Created-user-password-123!", created.getPasswordHash()));

        mockMvc.perform(post("/equipos")
                        .with(user("csrf-user@example.test").roles("USER"))
                        .with(csrf())
                        .param("name", "CSRF protected unit"))
                .andExpect(status().is3xxRedirection());
        org.junit.jupiter.api.Assertions.assertTrue(equipment.findAll().stream()
                .anyMatch(item -> "CSRF protected unit".equals(item.getName())));
    }

    @Test
    void technicianCanRecordReplacedPartsInTheSharedHistory() throws Exception {
        String email = "parts-tech@example.test";
        users.save(new AppUser(email, passwordEncoder.encode("Parts-tech-password-123!"), UserRole.USER));
        AirConditioner unit = equipment.save(new AirConditioner(
                "Parts test unit", "", "", "", "", "", null, "", null, ""));

        mockMvc.perform(post("/equipos/" + unit.getId() + "/servicios")
                        .with(user(email).roles("USER"))
                        .with(csrf())
                        .param("serviceDate", "2026-11-24")
                        .param("description", "Reemplazo de pieza")
                        .param("technician", "Técnico")
                        .param("observations", "Prueba de funcionamiento")
                        .param("partsReplaced", "Capacitor"))
                .andExpect(status().is3xxRedirection());

        org.junit.jupiter.api.Assertions.assertEquals("Capacitor",
                services.findAll().stream()
                        .filter(service -> "Reemplazo de pieza".equals(service.getDescription()))
                        .findFirst().orElseThrow().getPartsReplaced());
    }

    @Test
    void loginReturnsTechnicianToTheQrRequestedEquipment() throws Exception {
        var response = mockMvc.perform(get("/equipos/482"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        mockMvc.perform(post("/login")
                        .session((org.springframework.mock.web.MockHttpSession) response.getRequest().getSession())
                        .with(csrf())
                        .param("username", "bootstrap-test@example.test")
                        .param("password", BOOTSTRAP_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrlPattern("http://localhost/equipos/482*"));
    }
}
