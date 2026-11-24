package com.facundo.trazabilidad;

import com.facundo.trazabilidad.model.AirConditioner;
import com.facundo.trazabilidad.model.ServiceRecord;
import com.facundo.trazabilidad.repository.AirConditionerRepository;
import com.facundo.trazabilidad.repository.ServiceRecordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class TrazabilidadAiresApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrazabilidadAiresApplication.class, args);
    }

    @Bean
    CommandLineRunner loadExampleData(AirConditionerRepository equipmentRepository,
                                      ServiceRecordRepository serviceRepository) {
        return args -> {
            if (equipmentRepository.count() > 0) {
                return;
            }

            AirConditioner office = equipmentRepository.save(new AirConditioner(
                    "Oficina 1", "Samsung", "AR12", "24380XX", "12.000 BTU",
                    "R32", LocalDate.of(2025, 3, 15), "J. Pérez",
                    LocalDate.of(2027, 3, 15), "Minisplit"));
            AirConditioner livingRoom = equipmentRepository.save(new AirConditioner(
                    "Sala principal", "LG", "Dual Inverter", "LG-90821", "18.000 BTU",
                    "R410A", LocalDate.of(2024, 8, 2), "M. Gómez",
                    LocalDate.of(2026, 8, 2), "Minisplit"));

            serviceRepository.save(new ServiceRecord(office, LocalDate.of(2026, 11, 20),
                    "Limpieza de filtros y bandeja. Revisión de carga de gas.",
                    "J. Pérez", "Filtros limpios; presión dentro del rango normal."));
            serviceRepository.save(new ServiceRecord(office, LocalDate.of(2026, 8, 14),
                    "Cambio del capacitor del ventilador.", "J. Pérez",
                    "Equipo probado en frío y calor."));
            serviceRepository.save(new ServiceRecord(livingRoom, LocalDate.of(2026, 10, 5),
                    "Mantenimiento preventivo y limpieza de unidad exterior.",
                    "M. Gómez", "Se recomienda revisar el drenaje en la próxima visita."));
        };
    }
}
