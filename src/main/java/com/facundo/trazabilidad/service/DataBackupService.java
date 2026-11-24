package com.facundo.trazabilidad.service;

import com.facundo.trazabilidad.model.AirConditioner;
import com.facundo.trazabilidad.model.ServiceRecord;
import com.facundo.trazabilidad.repository.AirConditionerRepository;
import com.facundo.trazabilidad.repository.ServiceRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class DataBackupService {
    private static final int FORMAT_VERSION = 1;
    private static final int MAX_EQUIPMENT = 10_000;
    private static final int MAX_SERVICES = 100_000;

    private final AirConditionerRepository equipmentRepository;
    private final ServiceRecordRepository serviceRepository;
    private final ObjectMapper objectMapper;

    public DataBackupService(AirConditionerRepository equipmentRepository,
                             ServiceRecordRepository serviceRepository,
                             ObjectMapper objectMapper) {
        this.equipmentRepository = equipmentRepository;
        this.serviceRepository = serviceRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public byte[] exportJson() throws JsonProcessingException {
        var equipment = equipmentRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(item -> new DataBackup.EquipmentData(item.getId(), item.getName(), item.getBrand(),
                        item.getModel(), item.getSerialNumber(), item.getCapacity(), item.getRefrigerant(),
                        item.getInstallationDate(), item.getInstaller(), item.getWarrantyEndDate(),
                        item.getEquipmentType()))
                .toList();
        var services = serviceRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(item -> new DataBackup.ServiceData(item.getId(), item.getAirConditioner().getId(),
                        item.getServiceDate(), item.getDescription(), item.getTechnician(),
                        item.getObservations(), item.getPartsReplaced()))
                .toList();
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(new DataBackup(FORMAT_VERSION, equipment, services));
    }

    @Transactional
    public ImportResult importJson(byte[] content) throws IOException {
        DataBackup backup = objectMapper.readValue(content, DataBackup.class);
        validate(backup);
        if (equipmentRepository.count() != 0 || serviceRepository.count() != 0) {
            throw new IllegalStateException("La importación solo se permite cuando el inventario está vacío.");
        }

        Map<Long, AirConditioner> equipmentBySourceId = new HashMap<>();
        for (DataBackup.EquipmentData item : backup.equipment()) {
            AirConditioner equipment = equipmentRepository.save(new AirConditioner(
                    item.name(), item.brand(), item.model(), item.serialNumber(), item.capacity(),
                    item.refrigerant(), item.installationDate(), item.installer(),
                    item.warrantyEndDate(), item.equipmentType()));
            equipmentBySourceId.put(item.id(), equipment);
        }

        for (DataBackup.ServiceData item : backup.services()) {
            ServiceRecord service = new ServiceRecord(equipmentBySourceId.get(item.equipmentId()),
                    item.serviceDate(), item.description(), item.technician(), item.observations());
            service.setPartsReplaced(item.partsReplaced());
            serviceRepository.save(service);
        }

        return new ImportResult(backup.equipment().size(), backup.services().size());
    }

    private void validate(DataBackup backup) {
        if (backup == null || backup.version() != FORMAT_VERSION
                || backup.equipment() == null || backup.services() == null) {
            throw new IllegalArgumentException("El archivo no es un respaldo ClimaTrack compatible.");
        }
        if (backup.equipment().size() > MAX_EQUIPMENT || backup.services().size() > MAX_SERVICES) {
            throw new IllegalArgumentException("El respaldo supera el límite de registros permitido.");
        }

        Set<Long> equipmentIds = new HashSet<>();
        for (DataBackup.EquipmentData item : backup.equipment()) {
            if (item == null || item.id() == null || item.id() <= 0 || !equipmentIds.add(item.id())) {
                throw new IllegalArgumentException("El respaldo contiene identificadores de equipo inválidos.");
            }
            requireText(item.name(), 100, "identificación");
            validateOptional(item.brand(), 80, "marca");
            validateOptional(item.model(), 80, "modelo");
            validateOptional(item.serialNumber(), 80, "número de serie");
            validateOptional(item.capacity(), 50, "capacidad");
            validateOptional(item.refrigerant(), 40, "refrigerante");
            validateOptional(item.installer(), 100, "instalador");
            validateOptional(item.equipmentType(), 60, "tipo de equipo");
        }

        Set<Long> serviceIds = new HashSet<>();
        for (DataBackup.ServiceData item : backup.services()) {
            if (item == null || item.id() == null || item.id() <= 0 || !serviceIds.add(item.id())
                    || item.equipmentId() == null || !equipmentIds.contains(item.equipmentId())
                    || item.serviceDate() == null) {
                throw new IllegalArgumentException("El respaldo contiene una visita de servicio inválida.");
            }
            requireText(item.description(), 2000, "trabajo realizado");
            requireText(item.technician(), 100, "técnico");
            validateOptional(item.observations(), 2000, "observaciones");
            validateOptional(item.partsReplaced(), 2000, "repuestos");
        }
    }

    private void requireText(String value, int maxLength, String field) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new IllegalArgumentException("El campo " + field + " del respaldo no es válido.");
        }
    }

    private void validateOptional(String value, int maxLength, String field) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException("El campo " + field + " del respaldo supera el límite permitido.");
        }
    }

    public record ImportResult(int equipmentCount, int serviceCount) {
    }
}
