package com.wac.autocore.repository;

import com.wac.autocore.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    WorkOrder getWorkOrderById(Long id);
}
