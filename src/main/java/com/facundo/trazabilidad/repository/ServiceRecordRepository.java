package com.facundo.trazabilidad.repository;

import com.facundo.trazabilidad.model.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    List<ServiceRecord> findTop5ByOrderByServiceDateDescIdDesc();
    long countByAirConditionerId(Long airConditionerId);

}
