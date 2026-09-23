package com.wac.autocore.repository;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD
import java.util.List;

public class CustomerRepository {

    public List<Customer> findAll() {
        return Database.getCustomers();
    }

    public void save(Customer customer) {
        Database.getCustomers().add(customer);
    }


