package com.facundo.trazabilidad.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
public class ServiceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AirConditioner airConditioner;

    @NotNull(message = "La fecha del trabajo es obligatoria.")
    private LocalDate serviceDate;

    @NotBlank(message = "Describe el trabajo realizado.")
    @Size(max = 2000)
    private String description;

    @NotBlank(message = "Indica quién realizó el trabajo.")
    @Size(max = 100)
    private String technician;

    @Size(max = 2000)
    private String observations;

    @Size(max = 2000)
    private String partsReplaced;

    protected ServiceRecord() {
    }

    public ServiceRecord(AirConditioner airConditioner, LocalDate serviceDate,
                        String description, String technician, String observations) {
        this.airConditioner = airConditioner;
        this.serviceDate = serviceDate;
        this.description = description;
        this.technician = technician;
        this.observations = observations;
        this.partsReplaced = "";
    }

    public Long getId() { return id; }
    public AirConditioner getAirConditioner() { return airConditioner; }
    public LocalDate getServiceDate() { return serviceDate; }
    public String getDescription() { return description; }
    public String getTechnician() { return technician; }
    public String getObservations() { return observations; }
    public String getPartsReplaced() { return partsReplaced; }

    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }
    public void setDescription(String description) { this.description = description; }
    public void setTechnician(String technician) { this.technician = technician; }
    public void setObservations(String observations) { this.observations = observations; }
    public void setPartsReplaced(String partsReplaced) { this.partsReplaced = partsReplaced; }
}
