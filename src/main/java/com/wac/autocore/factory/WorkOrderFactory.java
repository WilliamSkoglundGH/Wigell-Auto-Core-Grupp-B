package com.wac.autocore.factory;

import com.wac.autocore.dto.workorder.CreateWorkOrderDto;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.WorkOrder;

public interface WorkOrderFactory {

    WorkOrder createPlanned(Booking booking);

    WorkOrder createDropIn(CreateWorkOrderDto dto);

    WorkOrder createClaim(WorkOrder originalWorkOrder, CreateWorkOrderDto dto);
}
