package com.wac.autocore.model;

import javax.persistence.*;

@Entity
@Table(name = "invoice_line")
public class InvoiceLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "name_of_service", length = 60, nullable = false)
    private String nameOfService;
    @Column(name = "amount", nullable = false)
    private double amount;
    @Column(name = "discount", nullable = false)
    private double discount;
}
