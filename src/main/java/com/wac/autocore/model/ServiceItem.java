package com.wac.autocore.model;
import javax.persistence.*;
@Entity
@Table(name = "service_item")
public class ServiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", length = 60, nullable = false)
    private String name;
    @Column(name = "description", length = 200, nullable = true)
    private String description;
    @Column(name = "price", nullable = false)
    private double price;
    @Column(name = "estimated_minutes", nullable = false)
    private int estimatedMinutes;

    protected ServiceItem() {
    }

    public ServiceItem(String name, String description, double price, int estimatedMinutes) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.estimatedMinutes = estimatedMinutes;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    @Override
    public String toString() {
        return id + " - " + name +
                " | Price: " + price + " SEK" +
                " | Description: " + description +
                " | Estimated time: " + estimatedMinutes + " min";
    }
}