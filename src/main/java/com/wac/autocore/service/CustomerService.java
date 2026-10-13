package com.wac.autocore.service;

import com.wac.autocore.dto.customer.CustomerCreateDto;
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
                .orElseThrow(() -> new CustomerNotFoundException("customer.error.not_found"));
    }

    @Transactional
    public Customer createCustomer(CustomerCreateDto dto) {

        // --- UI/DTO-validering (samma som tidigare, men med dto) ---
        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw new IllegalArgumentException("customer.error.enter_name");
        }

        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("customer.error.enter_email");
        }

        if (!dto.getEmail().contains("@") || !dto.getEmail().contains(".")) {
            throw new IllegalArgumentException("customer.error.invalid_email");
        }

        String phone = dto.getPhone();

        if (phone != null && !phone.isEmpty()) {

            String regex = "^(07[023679]\\d{7}|0\\d{6,10}|\\+467[023679]\\d{7})$";

            // Normalisera för validering: 070-1234567 → 0701234567
            String normalized = phone.replace("-", "");

            if (!normalized.matches(regex)) {
                throw new IllegalArgumentException("customer.error.invalid_phone");
            }

            if (normalized.length() == 10) { // t.ex. 0701234567
                phone = normalized.substring(0, 3) + "-" + normalized.substring(3);
            }
        }

        Customer customer = new Customer(dto.getName(), phone, dto.getEmail());
        customer.setVip(dto.isVip());

        repo.save(customer);

        return customer;
    }


}




