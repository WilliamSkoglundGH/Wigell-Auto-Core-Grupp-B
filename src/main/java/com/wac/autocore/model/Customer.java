package com.wac.autocore.model;

@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", length = 60, nullable = false)
    private String name;
    @Column(name = "phone", length = 20, nullable = true)
    private String phone;
    @Column(name = "email", length = 60, nullable = false)
    private String email;
    @Column(name="vip")
    private boolean vip;
    public Customer(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.vip = false;
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

    @Override
    public String toString() {
        return id + " - " + name +
                " | Phone: " + phone +
                " | Email: " + email +
                " | VIP: " + (vip ? "Yes" : "No");
    }
}