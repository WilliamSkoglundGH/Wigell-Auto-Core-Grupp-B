package com.wac.autocore.config;

import com.wac.autocore.model.*;
import com.wac.autocore.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(CustomerRepository customerRepository,
                               VehicleRepository vehicleRepository,
                               ServiceItemRepository serviceItemRepository,
                               MechanicRepository mechanicRepository,
                               BookingRepository bookingRepository) {
        return args -> {
            // Kör endast om databasen är tom
            if (customerRepository.count() == 0) {

                // 1. Skapa Kunder
                Customer customer1 = new Customer("Anna Andersson", "070-1111111", "anna.andersson@email.se");
                Customer customer2 = new Customer("Erik Eriksson", "070-2222222", "erik.eriksson@email.se");
                Customer customer3 = new Customer("Maria Svensson", "070-3333333", "maria.svensson@email.se");

                customer2.setVip(true);

                customerRepository.saveAll(Arrays.asList(customer1, customer2, customer3));

                // 2. Skapa Fordon
                Vehicle vehicle1 = new Vehicle("ABC123", "Volvo", "V70", 2012, customer1);
                Vehicle vehicle2 = new Vehicle("DEF456", "Volkswagen", "Passat", 2018, customer2);
                Vehicle vehicle3 = new Vehicle("GHI789", "Toyota", "Corolla", 2020, customer3);

                vehicleRepository.saveAll(Arrays.asList(vehicle1, vehicle2, vehicle3));

                // 3. Skapa Tjänster (ServiceItems)
                ServiceItem service1 = new ServiceItem("Oil change", "Engine oil and oil filter replacement", 1295.0, 45);
                ServiceItem service2 = new ServiceItem("Brake service", "Inspection and replacement of front brake pads", 2495.0, 90);
                ServiceItem service3 = new ServiceItem("Diagnostics", "Electronic fault code diagnostics", 995.0, 60);
                ServiceItem service4 = new ServiceItem("Annual service", "Standard annual vehicle service", 3495.0, 120);

                serviceItemRepository.saveAll(Arrays.asList(service1, service2, service3, service4));

                // 4. Skapa Mekaniker
                Mechanic mechanic1 = new Mechanic("Johan Karlsson", "070-5551111", "General service");
                Mechanic mechanic2 = new Mechanic("Sara Nilsson", "070-5552222", "Brakes");
                Mechanic mechanic3 = new Mechanic("Mikael Berg", "070-5553333", "Diagnostics");

                mechanicRepository.saveAll(Arrays.asList(mechanic1, mechanic2, mechanic3));

                // 5. Skapa Bokningar
                Booking booking1 = new Booking(vehicle1, LocalDate.now().plusDays(2), "Annual service and general inspection", "Bokad");
                Booking booking2 = new Booking(vehicle2, LocalDate.now().plusDays(4), "Noise from front brakes", "Bokad");

                bookingRepository.saveAll(Arrays.asList(booking1, booking2));

                System.out.println(">>> Testdatan har laddats in i databasen utan problem!");
            }
        };
    }
}