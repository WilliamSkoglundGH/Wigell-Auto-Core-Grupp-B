package com.wac.autocore.factory;

import com.wac.autocore.dto.workorder.WorkOrderCreateDto;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.WorkOrder;

public interface WorkOrderFactory {

    WorkOrder createPlanned(Booking booking);

    WorkOrder createDropIn(WorkOrderCreateDto dto);

    WorkOrder createClaim(WorkOrder originalWorkOrder, WorkOrderCreateDto dto);
}
