package com.wac.autocore.service;

import com.wac.autocore.model.Customer;
import com.wac.autocore.repository.CustomerRepository;

import java.util.List;

public class CustomerService {

    private final CustomerRepository repo = new CustomerRepository();

    public List<Customer> getAllCustomers() {
        return repo.findAll();
    }

    public Customer createCustomer(String name, String phone, String email, boolean vip) {

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("You need a valid e-mail");
        }

        if (phone != null && !phone.isEmpty() && phone.length() < 5) {
            throw new IllegalArgumentException("You need a valid phonenumber");
        }

        Customer customer = new Customer(name, phone, email);
        customer.setVip(vip);

        repo.save(customer);

        return customer;
    }
}



