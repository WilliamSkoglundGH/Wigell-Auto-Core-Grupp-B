package com.wac.autocore.service;

import com.wac.autocore.dto.customer.CustomerCreateDto;
import com.wac.autocore.dto.customer.CustomerSummaryDto;
import com.wac.autocore.exception.EntityNotFoundException;
import com.wac.autocore.mapper.CustomerMapper;
import com.wac.autocore.model.Customer;
import com.wac.autocore.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private static final Logger logger =
            LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<CustomerSummaryDto> getAllCustomers() {

        try {
            return CustomerMapper.toSummaryDtoList(repo.findAll());

        } catch (DataAccessException e) {

            logger.error(
                    "Method: getAllCustomers | Could not load customers",
                    e);

            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public CustomerSummaryDto getCustomer(Long id) {

        return CustomerMapper.toSummaryDto(
                repo.findById(id)
                        .orElseThrow(() -> {
                            logger.error(
                                    "Method: getCustomer | Could not find customer | Id:{}",
                                    id);

                            return new EntityNotFoundException(
                                    "customer.error.not_found");
                        }));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
    @Transactional
    public CustomerSummaryDto createCustomer(CustomerCreateDto dto) {

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {

            logger.error(
                    "Method: createCustomer | Customer name is null or empty");

            throw new IllegalArgumentException(
                    "customer.error.enter_name");
        }

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {

            logger.error(
                    "Method: createCustomer | Missing email");

            throw new IllegalArgumentException(
                    "customer.error.enter_email");
        }

        if (!dto.getEmail().contains("@")
                || !dto.getEmail().contains(".")) {

            logger.error(
                    "Method: createCustomer | Invalid email");

            throw new IllegalArgumentException(
                    "customer.error.invalid_email");
        }

        String phone = dto.getPhone();

        if (phone != null && !phone.trim().isEmpty()) {

            String normalized = phone.replace("-", "");

            String regex =
                    "^(07[023679]\\d{7}|0\\d{6,10}|\\+467[023679]\\d{7})$";

            if (!normalized.matches(regex)) {

                logger.error(
                        "Method: createCustomer | Invalid phone format | Numbers:{}",
                        normalized);

                throw new IllegalArgumentException(
                        "customer.error.invalid_phone");
            }

            if (normalized.length() == 10) {
                phone = normalized.substring(0, 3)
                        + "-"
                        + normalized.substring(3);
            }
        }

        Customer customer =
                new Customer(dto.getName(), phone, dto.getEmail());

        customer.setVip(dto.isVip());

        customer = repo.save(customer);

        logger.info(
                "Method: createCustomer | Success | CustomerId:{}",
                customer.getId());

        return new CustomerSummaryDto(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.isVip()
        );
    }
}




