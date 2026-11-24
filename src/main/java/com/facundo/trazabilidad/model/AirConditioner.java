package com.facundo.trazabilidad.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class AirConditioner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La ubicación o identificación es obligatoria.")
    @Size(max = 100)
    private String name;

    @Size(max = 80)
    private String brand;

    @Size(max = 80)
    private String model;

    @Size(max = 80)
    private String serialNumber;

    @Size(max = 50)
    private String capacity;

    @Size(max = 40)
    private String refrigerant;

    private LocalDate installationDate;

    @Size(max = 100)
    private String installer;

    private LocalDate warrantyEndDate;

    @Size(max = 60)
    private String equipmentType;

    @OneToMany(mappedBy = "airConditioner", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("serviceDate DESC, id DESC")
    private List<ServiceRecord> serviceRecords = new ArrayList<>();

    protected AirConditioner() {
    }

    public AirConditioner(String name, String brand, String model, String serialNumber,
                          String capacity, String refrigerant, LocalDate installationDate,
                          String installer, LocalDate warrantyEndDate, String equipmentType) {
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.serialNumber = serialNumber;
        this.capacity = capacity;
        this.refrigerant = refrigerant;
        this.installationDate = installationDate;
        this.installer = installer;
        this.warrantyEndDate = warrantyEndDate;
        this.equipmentType = equipmentType;
    }

    public void updateFrom(AirConditioner form) {
        name = form.name;
        brand = form.brand;
        model = form.model;
        serialNumber = form.serialNumber;
        capacity = form.capacity;
        refrigerant = form.refrigerant;
        installationDate = form.installationDate;
        installer = form.installer;
        warrantyEndDate = form.warrantyEndDate;
        equipmentType = form.equipmentType;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getSerialNumber() { return serialNumber; }
    public String getCapacity() { return capacity; }
    public String getRefrigerant() { return refrigerant; }
    public LocalDate getInstallationDate() { return installationDate; }
    public String getInstaller() { return installer; }
    public LocalDate getWarrantyEndDate() { return warrantyEndDate; }
    public String getEquipmentType() { return equipmentType; }
    public List<ServiceRecord> getServiceRecords() { return serviceRecords; }

    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public void setCapacity(String capacity) { this.capacity = capacity; }
    public void setRefrigerant(String refrigerant) { this.refrigerant = refrigerant; }
    public void setInstallationDate(LocalDate installationDate) { this.installationDate = installationDate; }
    public void setInstaller(String installer) { this.installer = installer; }
    public void setWarrantyEndDate(LocalDate warrantyEndDate) { this.warrantyEndDate = warrantyEndDate; }
    public void setEquipmentType(String equipmentType) { this.equipmentType = equipmentType; }
}
