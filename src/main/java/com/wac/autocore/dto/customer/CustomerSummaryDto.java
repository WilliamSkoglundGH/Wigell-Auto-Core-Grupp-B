package com.wac.autocore.dto.customer;

public class CustomerSummaryDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private boolean vip;

    public CustomerSummaryDto() {
    }

    public CustomerSummaryDto(Long id, String name, String email, String phone, boolean vip) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.vip = vip;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }
}

