package com.wac.autocore.dto.booking;

import com.wac.autocore.model.BookingServiceItem;
import com.wac.autocore.model.BookingState;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.Vehicle;


import java.time.LocalDate;
import java.util.List;

public class BookingCloneDto {
    // Dem som ska klonas ut - i vilket skede?
    private Long id;
    private Vehicle vehicle;
//    private LocalDate date;
//  private String description;
    private BookingState status;
    private Mechanic mechanic;
    private List<BookingServiceItem> serviceItems; //Hämta färska snapshots-
}
