package com.wac.autocore.repository;

import com.wac.autocore.model.WorkOrder;
import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository {
    List<WorkOrder> findByMechanic_Id(Long mechanicId);
    Optional<WorkOrder> findByBooking_Id(Long bookingId);
    WorkOrder save(WorkOrder workOrder);
    Optional<WorkOrder> findById(Long id);
    List<WorkOrder> findAll();
}