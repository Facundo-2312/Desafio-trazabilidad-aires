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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDate;
import java.util.Enumeration;

@Controller
@RequestMapping("/equipos")
public class EquipmentController {
    private final AirConditionerRepository equipmentRepository;
    private final ServiceRecordRepository serviceRepository;
    private final String publicBaseUrl;

    public EquipmentController(AirConditionerRepository equipmentRepository,
                               ServiceRecordRepository serviceRepository,
                               @Value("${app.public-base-url:}") String publicBaseUrl) {
        this.equipmentRepository = equipmentRepository;
        this.serviceRepository = serviceRepository;
        this.publicBaseUrl = publicBaseUrl.trim();
    }

    @InitBinder("equipment")
    void protectEquipmentFields(WebDataBinder binder) {
        binder.setDisallowedFields("id", "serviceRecords");
    }

    @GetMapping
    public String dashboard(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("query", q);
        model.addAttribute("equipment", q.isBlank()
                ? equipmentRepository.findAll(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.ASC, "name"))
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
        model.addAttribute("equipment", new AirConditioner("", "", "", "", "", "", null, "", null, ""));
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
        ServiceRecord savedService = new ServiceRecord(equipment, service.getServiceDate(),
                service.getDescription().trim(), service.getTechnician().trim(),
                service.getObservations() == null ? "" : service.getObservations().trim());
        savedService.setPartsReplaced(service.getPartsReplaced() == null
                ? "" : service.getPartsReplaced().trim());
        serviceRepository.save(savedService);
        redirect.addFlashAttribute("success", "La visita de mantenimiento quedó registrada.");
        return "redirect:/equipos/" + id + "#historial";
    }

    @GetMapping(value = "/{id}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public void qrCode(@PathVariable Long id, HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        getEquipment(id);
        String url = buildEquipmentUrl(request, id);
        try {
            var matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 1024, 1024);
            response.setHeader("Cache-Control", CacheControl.noCache().getHeaderValue());
            MatrixToImageWriter.writeToStream(matrix, "PNG", response.getOutputStream());
        } catch (WriterException exception) {
            throw new IllegalStateException("No se pudo generar el código QR.", exception);
        }
    }

    private String buildEquipmentUrl(HttpServletRequest request, Long id) {
        if (!publicBaseUrl.isBlank()) {
            return publicBaseUrl.replaceAll("/+$", "") + "/equipos/" + id;
        }

        String host = request.getServerName();
        if (isLoopbackHost(host)) {
            host = findLanAddress().orElse(host);
        }

        String port = request.getServerPort() == 80 || request.getServerPort() == 443
                ? "" : ":" + request.getServerPort();
        return request.getScheme() + "://" + host + port + "/equipos/" + id;
    }

    private boolean isLoopbackHost(String host) {
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host)
                || "0:0:0:0:0:0:0:1".equals(host);
    }

    private java.util.Optional<String> findLanAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(':') < 0) {
                        return java.util.Optional.of(address.getHostAddress());
                    }
                }
            }
        } catch (Exception ignored) {
            // Fall back to the requested host when no LAN address is available.
        }
        return java.util.Optional.empty();
    }

    private AirConditioner getEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Equipo no encontrado"));
    }
}
