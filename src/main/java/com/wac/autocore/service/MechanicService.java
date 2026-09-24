package com.wac.autocore.service;

import com.wac.autocore.exception.MechanicNotFoundException;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.repository.MechanicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MechanicService {
    private static final Logger logger = LoggerFactory.getLogger(MechanicService.class);
    private final MechanicRepository mechanicRepository;

    public MechanicService(MechanicRepository mechanicRepository) {
        this.mechanicRepository = mechanicRepository;
    }
    @Transactional(readOnly = true)
    public List<Mechanic> getAllMechanics() {
        return mechanicRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Mechanic getMechanic(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new MechanicNotFoundException("Mechanic with ID " + id + " not found."));
    }
}
