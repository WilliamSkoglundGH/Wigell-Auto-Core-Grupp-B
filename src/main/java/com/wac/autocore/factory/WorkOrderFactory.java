package com.wac.autocore.factory;

import com.wac.autocore.dto.workorder.WorkOrderCreateDto;
import com.wac.autocore.model.*;

import java.util.List;

public interface WorkOrderFactory {

    WorkOrder createPlanned(Booking booking);

    WorkOrder createDropIn(WorkOrderCreateDto dto, Vehicle vehicle,
                           Mechanic mechanic, List<ServiceItem> serviceItems);

    WorkOrder createClaim(WorkOrder originalWorkOrder, WorkOrderCreateDto dto);
}
