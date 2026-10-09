package com.wac.autocore.dto.servicePackage;

import com.wac.autocore.model.ServiceItem;

import java.util.List;

public class ServicePackageDetailDto {

    private Long id;
    private String name;
    private String description;
    private boolean active;
    private List<ServiceItem> serviceItems;
    public ServicePackageDetailDto(Long id, String name, String description, boolean active, List<ServiceItem> serviceItems) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.serviceItems = serviceItems;
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

    public List<ServiceItem> getServiceItems() {
        return serviceItems;
    }
}