package com.wac.autocore.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "work_order_services")
public class WorkOrderServiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_item_id", nullable = false)
    private ServiceItem serviceItem;
    @Column(name = "price_at_time", precision = 10, scale = 2, nullable = true)
    private BigDecimal priceAtTime;

    @Column(name = "duration_at_time", nullable = true)
    private int durationAtTime;

    protected WorkOrderServiceItem() {
    }

    public WorkOrderServiceItem(WorkOrder workOrder, ServiceItem serviceItem, BigDecimal priceAtTime, int durationAtTime) {
        this.workOrder = workOrder;
        this.serviceItem = serviceItem;
        this.priceAtTime = priceAtTime;
        this.durationAtTime = durationAtTime;
    }

    public Long getId() {
        return id;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public ServiceItem getServiceItem() {
        return serviceItem;
    }

    public void setServiceItem(ServiceItem serviceItem) {
        this.serviceItem = serviceItem;
    }

    public BigDecimal getPriceAtTime() {
        return priceAtTime;
    }

    public void setPriceAtTime(BigDecimal priceAtTime) {
        this.priceAtTime = priceAtTime;
    }

    public int getDurationAtTime() {
        return durationAtTime;
    }

    public void setDurationAtTime(int durationAtTime) {
        this.durationAtTime = durationAtTime;
    }
}
