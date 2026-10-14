package com.wac.autocore.dto.workorder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkOrderCreateDto {
    private Long vehicleId;
    private Long mechanicId;
    private LocalDate plannedDate;
    private String description;
    private String customerInstructions;
    private List<Long> serviceItemsIds = new ArrayList<>();

    public WorkOrderCreateDto() {
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getMechanicId() {
        return mechanicId;
    }

    public void setMechanicId(Long mechanicId) {
        this.mechanicId = mechanicId;
    }

    public LocalDate getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(LocalDate plannedDate) {
        this.plannedDate = plannedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCustomerInstructions() {
        return customerInstructions;
    }

    public void setCustomerInstructions(String customerInstructions) {
        this.customerInstructions = customerInstructions;
    }

    public List<Long> getServiceItemsIds() {
        return serviceItemsIds;
    }

    public void setServiceItemsIds(List<Long> serviceItemsIds) {
        this.serviceItemsIds = serviceItemsIds;
    }
}
