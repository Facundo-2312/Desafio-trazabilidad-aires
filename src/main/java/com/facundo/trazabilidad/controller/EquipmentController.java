package com.facundo.trazabilidad.controller;

import com.facundo.trazabilidad.model.AirConditioner;
import com.facundo.trazabilidad.model.ServiceRecord;
import com.facundo.trazabilidad.repository.AirConditionerRepository;
import com.facundo.trazabilidad.repository.ServiceRecordRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/equipos")
public class EquipmentController {
    private final AirConditionerRepository equipmentRepository;
    private final ServiceRecordRepository serviceRepository;

    public EquipmentController(AirConditionerRepository equipmentRepository,
                               ServiceRecordRepository serviceRepository) {
        this.equipmentRepository = equipmentRepository;
        this.serviceRepository = serviceRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/equipos";
    }

    @InitBinder("equipment")
    void protectEquipmentFields(WebDataBinder binder) {
        binder.setDisallowedFields("id", "serviceRecords");
    }

    @GetMapping
    public String dashboard(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("query", q);
        model.addAttribute("equipment", q.isBlank()
                ? equipmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                : equipmentRepository
                    .findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrSerialNumberContainingIgnoreCase(
                            q, q, q));
        model.addAttribute("equipmentCount", equipmentRepository.count());
        model.addAttribute("serviceCount", serviceRepository.count());
        model.addAttribute("recentServices", serviceRepository.findTop5ByOrderByServiceDateDescIdDesc());
        return "dashboard";
    }

    @GetMapping("/nuevo")
    public String newEquipment(Model model) {
        model.addAttribute("equipment", new AirConditioner("", "", "", "", "", "",
                null, "", null, ""));
        model.addAttribute("pageTitle", "Agregar equipo");
        return "equipment-form";
    }

    @PostMapping
    public String createEquipment(@Valid @ModelAttribute("equipment") AirConditioner equipment,
                                  BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Agregar equipo");
            return "equipment-form";
        }
        AirConditioner saved = equipmentRepository.save(equipment);
        redirect.addFlashAttribute("success", "El equipo se registró correctamente.");
        return "redirect:/equipos/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        AirConditioner equipment = getEquipment(id);
        model.addAttribute("equipment", equipment);
        model.addAttribute("serviceForm", new ServiceRecord(equipment, LocalDate.now(), "", "", ""));
        model.addAttribute("serviceCount", serviceRepository.countByAirConditionerId(id));
        return "equipment-detail";
    }

    @GetMapping("/{id}/editar")
    public String editEquipment(@PathVariable Long id, Model model) {
        model.addAttribute("equipment", getEquipment(id));
        model.addAttribute("pageTitle", "Editar equipo");
        return "equipment-form";
    }

    @PostMapping("/{id}/editar")
    public String updateEquipment(@PathVariable Long id,
                                  @Valid @ModelAttribute("equipment") AirConditioner form,
                                  BindingResult result, Model model, RedirectAttributes redirect) {
        AirConditioner current = getEquipment(id);
        if (result.hasErrors()) {
            model.addAttribute("equipment", form);
            model.addAttribute("pageTitle", "Editar equipo");
            return "equipment-form";
        }
        current.updateFrom(form);
        equipmentRepository.save(current);
        redirect.addFlashAttribute("success", "Los datos del equipo se actualizaron.");
        return "redirect:/equipos/" + id;
    }

    @PostMapping("/{id}/servicios")
    public String addService(@PathVariable Long id,
                             @Valid @ModelAttribute("serviceForm") ServiceRecord service,
                             BindingResult result, Model model, RedirectAttributes redirect) {
        AirConditioner equipment = getEquipment(id);
        if (result.hasErrors()) {
            model.addAttribute("equipment", equipment);
            model.addAttribute("serviceCount", serviceRepository.countByAirConditionerId(id));
            return "equipment-detail";
        }
        serviceRepository.save(new ServiceRecord(equipment, service.getServiceDate(),
                service.getDescription().trim(), service.getTechnician().trim(),
                service.getObservations() == null ? "" : service.getObservations().trim()));
        redirect.addFlashAttribute("success", "La visita de mantenimiento quedó registrada.");
        return "redirect:/equipos/" + id + "#historial";
    }

    @GetMapping(value = "/{id}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public void qrCode(@PathVariable Long id, HttpServletResponse response) throws IOException {
        getEquipment(id);
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/equipos/{id}").buildAndExpand(id).toUriString();
        try {
            var matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 420, 420);
            response.setHeader("Cache-Control", CacheControl.noCache().getHeaderValue());
            MatrixToImageWriter.writeToStream(matrix, "PNG", response.getOutputStream());
        } catch (WriterException exception) {
            throw new IllegalStateException("No se pudo generar el código QR.", exception);
        }
    }

    private AirConditioner getEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Equipo no encontrado"));
    }
}
