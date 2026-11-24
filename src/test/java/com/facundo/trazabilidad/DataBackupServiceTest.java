package com.facundo.trazabilidad;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.facundo.trazabilidad.repository.AirConditionerRepository;
import com.facundo.trazabilidad.repository.ServiceRecordRepository;
import com.facundo.trazabilidad.service.DataBackup;
import com.facundo.trazabilidad.service.DataBackupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:backup-tests;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.example-data.enabled=false",
        "app.auth.bootstrap-admin.email=backup-test@example.test",
        "app.auth.bootstrap-admin.password=Test-Only-Backup-Password-478!"
})
class DataBackupServiceTest {
    @Autowired
    private DataBackupService backupService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AirConditionerRepository equipmentRepository;
    @Autowired
    private ServiceRecordRepository serviceRepository;

    @Test
    void importsAndExportsEquipmentWithServiceParts() throws Exception {
        DataBackup backup = new DataBackup(1,
                List.of(new DataBackup.EquipmentData(8L, "Oficina", "Marca", "Modelo", "Serie",
                        "12000 BTU", "R32", LocalDate.of(2025, 3, 15), "Instalador",
                        LocalDate.of(2027, 3, 15), "Minisplit")),
                List.of(new DataBackup.ServiceData(11L, 8L, LocalDate.of(2026, 11, 20),
                        "Cambio de componente", "Técnico", "Prueba realizada", "Capacitor")));

        DataBackupService.ImportResult result = backupService.importJson(objectMapper.writeValueAsBytes(backup));

        assertEquals(1, result.equipmentCount());
        assertEquals(1, result.serviceCount());
        assertEquals("Capacitor", serviceRepository.findAll().get(0).getPartsReplaced());
        JsonNode exported = objectMapper.readTree(backupService.exportJson());
        assertEquals("Oficina", exported.path("equipment").get(0).path("name").asText());
        assertEquals("Capacitor", exported.path("services").get(0).path("partsReplaced").asText());
        assertThrows(IllegalStateException.class,
                () -> backupService.importJson(objectMapper.writeValueAsBytes(backup)));
    }

    @Test
    void rejectsBrokenServiceReferencesWithoutWritingPartialData() throws Exception {
        DataBackup backup = new DataBackup(1,
                List.of(new DataBackup.EquipmentData(3L, "Oficina", "", "", "", "", "",
                        null, "", null, "")),
                List.of(new DataBackup.ServiceData(4L, 99L, LocalDate.now(),
                        "Trabajo", "Técnico", "", "")));

        assertThrows(IllegalArgumentException.class,
                () -> backupService.importJson(objectMapper.writeValueAsBytes(backup)));
        assertTrue(equipmentRepository.findAll().isEmpty());
        assertTrue(serviceRepository.findAll().isEmpty());
    }
}
