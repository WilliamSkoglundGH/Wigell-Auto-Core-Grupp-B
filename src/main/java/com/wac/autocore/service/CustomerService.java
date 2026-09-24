package com.wac.autocore.service;

import com.wac.autocore.exception.CustomerNotFoundException;
import com.wac.autocore.model.Customer;
import com.wac.autocore.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Customer getCustomer(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with ID " + id + " not found."));
    }

    @Transactional
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

        if (phone != null && !phone.isEmpty()) {

            String regex = "^(07[023679]\\d{7}|0\\d{6,10}|\\+467[023679]\\d{7})$";

            // Normalisera för validering: 070-1234567 → 0701234567
            String normalized = phone.replace("-", "");

            if (!normalized.matches(regex)) {
                throw new IllegalArgumentException("Invalid Swedish phone number");
            }

            if (normalized.length() == 10) { // t.ex. 0701234567
                phone = normalized.substring(0, 3) + "-" + normalized.substring(3);
            }
        }

        Customer customer = new Customer(name, phone, email);
        customer.setVip(vip);

        repo.save(customer);

        return customer;
    }

}




