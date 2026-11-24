package com.facundo.trazabilidad.service;

import java.time.LocalDate;
import java.util.List;

public record DataBackup(int version, List<EquipmentData> equipment, List<ServiceData> services) {
    public record EquipmentData(
            Long id,
            String name,
            String brand,
            String model,
            String serialNumber,
            String capacity,
            String refrigerant,
            LocalDate installationDate,
            String installer,
            LocalDate warrantyEndDate,
            String equipmentType) {
    }

    public record ServiceData(
            Long id,
            Long equipmentId,
            LocalDate serviceDate,
            String description,
            String technician,
            String observations,
            String partsReplaced) {
    }
}
