package com.wac.autocore.service.WorkOrderFactory;

import com.wac.autocore.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultWorkOrderFactory implements WorkOrderFactory {

    @Override
    public WorkOrder createBooked(Booking booking) {
        WorkOrder wo = new WorkOrder();
        wo.setType(WorkOrderType.BOOKED);
        wo.setStatus(WorkOrderState.CONFIRMED);
        wo.setBooking(booking);

        List<WorkOrderServiceItem> items = new ArrayList<>();

        if (booking.getServiceItems() != null) {
            for (BookingServiceItem bsi : booking.getServiceItems()) {
                WorkOrderServiceItem wosi = new WorkOrderServiceItem(
                                wo,
                                bsi.getServiceItem(),
                                bsi.getPriceAtTime(),
                                bsi.getDurationAtTime()
                        );

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
        wo.setStatus(WorkOrderState.DRAFT);
        wo.setCustomer(customer);
        wo.setVehicle(vehicle);

        List<WorkOrderServiceItem> items = new ArrayList<>();

        if (serviceItems != null) {
            for (ServiceItem si : serviceItems) {

                WorkOrderServiceItem wosi =
                        new WorkOrderServiceItem(
                                wo,
                                si,                     // ServiceItem-referensen
                                si.getPrice(),          // aktuellt pris
                                si.getEstimatedMinutes()         // aktuell tid
                        );

                items.add(wosi);
            }
        }
        wo.setServiceItems(items);
        return wo;
    }


    @Override
    public WorkOrder createClaim(WorkOrder original, String reason) {

        // CLAIM konstruktor
        WorkOrder wo = new WorkOrder(original, WorkOrderType.CLAIM);

        // CLAIM startar alltid i DRAFT eftersom den är bara rappel
        wo.setStatus(WorkOrderState.DRAFT);

        // Kopiera kund och fordon från originalet
        wo.setCustomer(original.getCustomer());
        wo.setVehicle(original.getVehicle());

        // Sätt reklamationsorsak
        wo.setClaimReason(reason);

        // Reklamation startar utan tjänster
        wo.setServiceItems(new ArrayList<>());

        return wo;
    }

}



