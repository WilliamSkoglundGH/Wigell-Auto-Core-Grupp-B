package com.wac.autocore.repository;

import com.wac.autocore.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findByMechanic_Id(Long mechanicId);
    Optional<WorkOrder> findByBooking_Id(Long bookingId);

}
