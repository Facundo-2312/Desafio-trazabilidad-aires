package com.facundo.trazabilidad.controller;

import com.facundo.trazabilidad.service.DataBackupService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin/data")
public class AdminDataController {
    private static final long MAX_BACKUP_SIZE = 10 * 1024 * 1024;
    private final DataBackupService backupService;

    public AdminDataController(DataBackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping
    public String dataTools() {
        return "admin-data";
    }

    @GetMapping("/backup.json")
    public ResponseEntity<byte[]> downloadBackup() throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("climatrack-backup.json").build().toString())
                .body(backupService.exportJson());
    }

    @PostMapping("/import")
    public String importBackup(@RequestParam("backup") MultipartFile backup,
                               RedirectAttributes redirect) {
        if (backup.isEmpty() || backup.getSize() > MAX_BACKUP_SIZE) {
            redirect.addFlashAttribute("error", "Elige un respaldo JSON válido de hasta 2 MB.");
            return "redirect:/admin/data";
        }
        try {
            var result = backupService.importJson(backup.getBytes());
            redirect.addFlashAttribute("success", "Importados "
                    + result.equipmentCount() + " equipos y "
                    + result.serviceCount() + " servicios.");
        } catch (IOException | IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", "No se pudo importar: el archivo no es válido o no es compatible.");
        } catch (IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/data";
    }
}
