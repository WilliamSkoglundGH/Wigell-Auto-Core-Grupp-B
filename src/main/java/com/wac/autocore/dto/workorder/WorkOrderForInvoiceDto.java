package com.wac.autocore.dto.workorder;

import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.model.enums.WorkOrderState;

public class WorkOrderForInvoiceDto {
    private final Long id;
    private final Long bookingId;
    private final String vehicleRegistrationNumber;
    private final String customerName;
    private final WorkOrderState STATUS;

    public WorkOrderForInvoiceDto(Long id, Long bookingId, String vehicleRegistrationNumber, String customerName, WorkOrderState STATUS) {
        this.id = id;
        this.bookingId = bookingId;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
        this.customerName = customerName;
        this.STATUS = STATUS;
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public WorkOrderState getSTATUS() {
        return STATUS;
    }
}
