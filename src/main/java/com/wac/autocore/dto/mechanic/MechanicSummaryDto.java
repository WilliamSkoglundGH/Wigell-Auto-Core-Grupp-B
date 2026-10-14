package com.wac.autocore.dto.mechanic;

import javax.persistence.Column;

public class MechanicSummaryDto {
    private Long id;
    private String name;
    private String phone;
    private String specialization;

    public MechanicSummaryDto() {
    }

    public MechanicSummaryDto(Long id, String name, String phone, String specialization) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.specialization = specialization;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
