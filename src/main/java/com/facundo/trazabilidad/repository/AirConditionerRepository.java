package com.facundo.trazabilidad.repository;

import com.facundo.trazabilidad.model.AirConditioner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AirConditionerRepository extends JpaRepository<AirConditioner, Long> {
    List<AirConditioner> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrSerialNumberContainingIgnoreCase(
            String name, String brand, String serialNumber);
}
