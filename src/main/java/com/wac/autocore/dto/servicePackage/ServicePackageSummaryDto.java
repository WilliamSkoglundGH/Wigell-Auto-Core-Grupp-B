package com.wac.autocore.dto.servicePackage;

public class ServicePackageSummaryDto {

    private Long id;
    private String name;
    private String description;
    private boolean active;
    private String services;

    public ServicePackageSummaryDto(Long id, String name, String description, boolean active, String services) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.services = services;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public String getServices() { // Returnerar nu en String
        return services;
    }
}