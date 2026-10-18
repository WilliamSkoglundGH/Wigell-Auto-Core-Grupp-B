package com.wac.autocore.dto.customer;

public class CustomerCreateDto {

    private String name;
    private String phone;
    private String email;
    private boolean vip;

    public CustomerCreateDto() {
    }

    public CustomerCreateDto(String name, String phone, String email, boolean vip) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.vip = vip;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }
}
