package com.wac.autocore.repository;

import com.wac.autocore.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findByBooking_Mechanic_Id(Long mechanicId);

}
