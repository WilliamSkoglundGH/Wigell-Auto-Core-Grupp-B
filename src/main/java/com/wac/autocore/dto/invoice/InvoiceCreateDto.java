package com.wac.autocore.dto.invoice;

public class InvoiceCreateDto {

    private Long workOrderId;
    private String discountCode;

    public InvoiceCreateDto() {
    }

    public InvoiceCreateDto(Long workOrderId, String discountCode) {
        this.workOrderId = workOrderId;
        this.discountCode = discountCode;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }
}
