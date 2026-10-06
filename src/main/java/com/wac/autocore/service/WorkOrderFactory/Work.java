package com.wac.autocore.service.WorkOrderFactory;

package com.wac.autocore.factory;

import com.wac.autocore.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultWorkOrderFactory implements WorkOrderFactory {

    @Override
    public WorkOrder createPlanned(Booking booking) {
        WorkOrder wo = new WorkOrder();
        // settype i nya workorder och draft som state i Enum
        wo.setType(WorkOrderType.PLANNED);
        wo.setState(WorkOrderState.DRAFT);
        wo.setBooking(booking);

        // Snapshot från booking till WorkOrderServiceItem
        List<WorkOrderServiceItem> items = new ArrayList<>();
        if (booking.getServiceItems() != null) {
            for (BookingServiceItem bsi : booking.getServiceItems()) {
                WorkOrderServiceItem wosi = new WorkOrderServiceItem();
                wosi.setWorkOrder(wo);
                wosi.setName(bsi.getName());
                wosi.setPriceAtTime(bsi.getPriceAtBooking());
                wosi.setDurationAtTime(bsi.getMinutesAtBooking());
                items.add(wosi);
            }
        }

        wo.setServiceItems(items);
        return wo;
    }

    @Override
    public WorkOrder createDropIn(Customer customer, Vehicle vehicle, List<ServiceItem> serviceItems) {
        WorkOrder wo = new WorkOrder();
        wo.setType(WorkOrderType.DROP_IN);
        wo.setState(WorkOrderState.DRAFT);
        wo.setCustomer(customer);
        wo.setVehicle(vehicle);

        List<WorkOrderServiceItem> items = new ArrayList<>();
        if (serviceItems != null) {
            for (ServiceItem si : serviceItems) {
                WorkOrderServiceItem wosi = new WorkOrderServiceItem();
                wosi.setWorkOrder(wo);
                wosi.setName(si.getName());
                wosi.setPriceAtTime(si.getPrice());
                wosi.setDurationAtTime(si.getMinutes());
                items.add(wosi);
            }
        }

        wo.setServiceItems(items);
        return wo;
    }

    @Override
    public WorkOrder createClaim(WorkOrder original, String reason) {
        WorkOrder wo = new WorkOrder();
        wo.setType(WorkOrderType.CLAIM);
        wo.setState(WorkOrderState.DRAFT);

        // Kopiera kund och fordon från ursprunglig arbetsorder
        wo.setCustomer(original.getCustomer());
        wo.setVehicle(original.getVehicle());

        // Koppla reklamationen till ursprunglig arbetsorder
        wo.setOriginalWorkOrder(original);
        // Ska man ha mer än en string eller räcker det ?
        wo.setClaimReason(reason);

        // Reklamation börjar utan tjänster i som grundläge ?
        wo.setServiceItems(new ArrayList<>());

        return wo;
    }
}

