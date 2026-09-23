package com.wac.autocore.service;

import com.wac.autocore.model.Payment;
import com.wac.autocore.repository.PaymentRepository;
import com.wac.autocore.service.payment.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    private final Map<String, PaymentStrategy> strategies;
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;

    public PaymentService(Map<String, PaymentStrategy> strategies, PaymentRepository paymentRepository) {
        this.strategies = strategies;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}
