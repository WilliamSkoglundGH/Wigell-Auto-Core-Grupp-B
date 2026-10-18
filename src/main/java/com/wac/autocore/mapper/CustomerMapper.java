package com.wac.autocore.mapper;

import com.wac.autocore.dto.customer.CustomerSummaryDto;
import com.wac.autocore.model.Customer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CustomerMapper {

    private CustomerMapper() {
        // Privat konstruktor för att förhindra instansiering av utility-klass
    }

    public static CustomerSummaryDto toSummaryDto(Customer customer) {
        if (customer == null) {
            return null;
        }

        return new CustomerSummaryDto(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.isVip()
        );
    }

    public static List<CustomerSummaryDto> toSummaryDtoList(List<Customer> customers) {
        if (customers == null || customers.isEmpty()) {
            return Collections.emptyList();
        }

        return customers.stream()
                .map(CustomerMapper::toSummaryDto)
                .collect(Collectors.toList());
    }
}