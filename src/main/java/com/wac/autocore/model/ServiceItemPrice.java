package com.wac.autocore.model;

import com.wac.autocore.model.ServiceItem;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "service_item_price")
public class ServiceItemPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_item_id")
    private ServiceItem serviceItem;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo; // null = aktiv version

    protected ServiceItemPrice() {}

    public ServiceItemPrice(ServiceItem serviceItem, BigDecimal price, LocalDate validFrom) {
        this.serviceItem = serviceItem;
        this.price = price;
        this.validFrom = validFrom;
    }

    public ServiceItem getServiceItem() {
        return serviceItem;
    }

    public void setServiceItem(ServiceItem serviceItem) {
        this.serviceItem = serviceItem;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }
}


