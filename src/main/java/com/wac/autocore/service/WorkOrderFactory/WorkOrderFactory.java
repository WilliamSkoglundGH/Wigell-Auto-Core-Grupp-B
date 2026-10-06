package com.wac.autocore.service.WorkOrderFactory;

import com.wac.autocore.model.*;

import java.util.List;

public interface WorkOrderFactory {
    WorkOrder createPlanned(Booking booking);
    WorkOrder createDropIn(Customer customer, Vehicle vehicle, List<ServiceItem> items);
    WorkOrder createClaim(WorkOrder original, String reason);
}

