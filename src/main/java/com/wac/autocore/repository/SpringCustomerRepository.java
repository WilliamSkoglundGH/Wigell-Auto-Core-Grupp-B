package com.wac.autocore.repository;

import com.wac.autocore.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringCustomerRepository extends CustomerRepository, JpaRepository<Customer, Long> {

    @Override
    Optional<Customer> findById(Long id);
}