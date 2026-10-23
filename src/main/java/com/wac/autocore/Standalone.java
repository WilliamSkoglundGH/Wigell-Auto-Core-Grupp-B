package com.wac.autocore;

import com.wac.autocore.dto.customer.CustomerCreateDto;
import com.wac.autocore.model.Customer;
import com.wac.autocore.repository.CustomerRepository;
import com.wac.autocore.repository.JdbcCustomerRepository;
import com.wac.autocore.repository.JdbcWorkOrderRepository;
import com.wac.autocore.repository.WorkOrderRepository;
import com.wac.autocore.service.CustomerService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Standalone {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- Wigell Auto Core (Fristående JDBC-läge) ---");
        System.out.println("--- VÄLJ BEHÖRIGHET FÖR DET FRISTÅENDE LÄGET ---");
        System.out.println("1. ADMIN");
        System.out.println("2. MECHANIC");
        System.out.println("3. READ_ONLY");
        System.out.print("Välj roll (1-3, annat ger roll IMPOSTER): ");
        String role = scanner.nextLine();
        String currentRole;
        switch (role) {
            case "1":
                currentRole = "ADMIN";
                break;
            case "2":
                currentRole = "MECHANIC";
                break;
            case "3":
                currentRole = "READ_ONLY";
                break;
            default:
                currentRole = "IMPOSTER";
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/wigell_autocore_db?useSSL=false&serverTimezone=UTC";
            Connection connection = DriverManager.getConnection(url, "root", "root");

            CustomerRepository customerRepo = new JdbcCustomerRepository(connection);
            CustomerService customerService = new CustomerService(customerRepo);


            boolean running = true;
            while (running) {
                System.out.println("\n=== HUVUDMENY ===");
                System.out.println("1. Hantera Kunder (Skapa, visa, söka)");
                System.out.println("2. Hantera Arbetsorder & Livscykel (Starta, slutföra)");
                System.out.println("3. Avsluta programmet");
                System.out.print("Val: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        runCustomerMenu(connection, currentRole, scanner);
                        break;
                    case "2":
                        runWorkOrderMenu(connection, currentRole, scanner);
                        break;
                    case "3":
                        running = false;
                        System.out.println("Avslutar fristående program...");
                        break;
                    default:
                        System.out.println("Ogiltigt val.");
                }
            }
        } catch (Exception e) {
            System.err.println("Ett fel inträffade i det fristående programmet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runCustomerMenu(Connection connection, String currentRole, Scanner scanner) {
        CustomerRepository customerRepo = new JdbcCustomerRepository(connection);
        CustomerService customerService = new CustomerService(customerRepo);


        boolean running = true;
        while (running) {
            System.out.println("\nVälj ett alternativ:");
            System.out.println("1. Visa alla kunder");
            System.out.println("2. Skapa ny kund");
            System.out.println("3. Sök kund på ID");
            System.out.println("4. Tillbaka");
            System.out.print("Val: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    if (!hasAccess(currentRole, "ADMIN", "MECHANIC", "READ_ONLY")) {
                        System.out.println("\n[SÄKERHETSBARRIÄR] Åtkomst nekad! Rollen '" + currentRole + "' saknar behörighet att visa kunder.");
                        continue; // Hoppa tillbaka till menyn
                    }
                    System.out.println("\n--- Alla kunder i databasen ---");
                    var customers = customerRepo.findAll();
                    if (customers.isEmpty()) {
                        System.out.println("Inga kunder hittades.");
                    } else {
                        customers.forEach(c -> System.out.println("- " + c.getName() + " (" + c.getEmail() + ", " + c.getPhone() + "), VIP: " + c.isVip()));
                    }
                    break;

                case "2":

                    if (!hasAccess(currentRole, "ADMIN")) {
                        System.out.println("\n[SÄKERHETSBARRIÄR] Åtkomst nekad! Rollen '" + currentRole + "' saknar behörighet att skapa kunder.");
                        continue; // Hoppa tillbaka till menyn
                    }
                    System.out.print("Ange namn: ");
                    String name = scanner.nextLine();
                    System.out.print("Ange telefon: ");
                    String phone = scanner.nextLine();
                    System.out.print("Ange e-post: ");
                    String email = scanner.nextLine();
                    System.out.print("Är kunden VIP? (true/false): ");
                    boolean vip = Boolean.parseBoolean(scanner.nextLine());

                    CustomerCreateDto dto = new CustomerCreateDto(name, phone, email, vip);
                    customerService.createCustomer(dto);
                    System.out.println("Kunden har sparats framgångsrikt!");
                    break;

                case "3":

                    if (!hasAccess(currentRole, "ADMIN", "MECHANIC", "READ_ONLY")) {
                        System.out.println("\n[SÄKERHETSBARRIÄR] Åtkomst nekad! Rollen '" + currentRole + "' saknar behörighet att visa kund.");
                        continue; // Hoppa tillbaka till menyn
                    }
                    System.out.print("Ange ID att söka på: ");
                    Long id;
                    try {
                        id = Long.parseLong(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Inte ett korrekt id");
                        continue;
                    }
                    var customerOpt = customerRepo.findById(id);
                    if (customerOpt.isPresent()) {
                        Customer c = customerOpt.get();
                        System.out.println("Hittade kund: " + c.getName() + " (" + c.getEmail() + ", " + c.getPhone() + "), VIP: " + c.isVip());
                    } else {
                        System.out.println("Ingen kund hittades med ID: " + id);
                    }
                    break;

                case "4":
                    running = false;
                    break;

                default:
                    System.out.println("Ogiltigt val, försök igen.");
            }
        }

    }

    private static void runWorkOrderMenu(Connection connection, String currentRole, Scanner scanner) {

        WorkOrderRepository workOrderRepo = new JdbcWorkOrderRepository(connection);

        boolean running = true;

        while (running) {
            System.out.println("\n=== ARBETSORDER-MENY ===");


            System.out.println("\nVälj åtgärd:");
            System.out.println("1. Starta arbetsorder (Start)");
            System.out.println("2. Slutför arbetsorder (Complete)");
            System.out.println("3. Skriv ut alla arbetsordrar");
            System.out.println("4. Tillbaka");
            System.out.print("Val: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    if (!hasAccess(currentRole, "ADMIN", "MECHANIC")) {
                        System.out.println("\n[SÄKERHETSBARRIÄR] Åtkomst nekad! Rollen '" + currentRole + "' saknar behörighet att starta arbetsorder.");
                        continue;
                    }
                    System.out.print("Ange ID på arbetsorder att STARTA: ");
                    Long id;
                    try {
                        id = Long.parseLong(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Inte ett korrekt id");
                        continue;
                    }
                    workOrderRepo.findById(id).ifPresentOrElse(wo -> {
                        if (wo.getStatus().canStart()) {
                            wo.setStatus(wo.getStatus().getNext());
                            wo.setStartTime(LocalDateTime.now());
                            workOrderRepo.save(wo);
                            System.out.println(">>> Arbetsorder " + id + " har startats!");
                        } else {
                            System.out.println(">>> Fel: Denna arbetsorder kan inte startas från nuvarande status (" + wo.getStatus() + ").");
                        }
                    }, () -> System.out.println(">>> Hittade ingen arbetsorder med det ID:t."));
                    break;
                case "2":
                    if (!hasAccess(currentRole, "ADMIN", "MECHANIC")) {
                        System.out.println("\n[SÄKERHETSBARRIÄR] Åtkomst nekad! Rollen '" + currentRole + "' saknar behörighet att avsluta arbetsorder.");
                        continue; // Hoppa tillbaka till menyn
                    }
                    System.out.print("Ange ID på arbetsorder att SLUTFÖRA: ");
                    try {
                        id = Long.parseLong(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Inte ett korrekt id");
                        continue;
                    }

                    workOrderRepo.findById(id).ifPresentOrElse(wo -> {
                        if (wo.getStatus().canComplete()) {
                            wo.setStatus(wo.getStatus().getNext());
                            wo.setEndTime(LocalDateTime.now());
                            workOrderRepo.save(wo);
                            System.out.println(">>> Arbetsorder " + id + " har slutförts!");
                        } else {
                            System.out.println(">>> Fel: Denna arbetsorder kan inte slutföras från nuvarande status (" + wo.getStatus() + ").");
                        }
                    }, () -> System.out.println(">>> Hittade ingen arbetsorder med det ID:t."));
                    break;
                case "3":
                    if (!hasAccess(currentRole, "ADMIN", "MECHANIC", "READ_ONLY")) {
                        System.out.println("\n[SÄKERHETSBARRIÄR] Åtkomst nekad! Rollen '" + currentRole + "' saknar behörighet att skriva ut arbetsordrar.");
                        continue;
                    }
                    var orders = workOrderRepo.findAll();
                    if (orders.isEmpty()) {
                        System.out.println(" (Inga arbetsorder hittades)");
                    } else {
                        orders.forEach(wo -> System.out.println("ID: " + wo.getId() + " | Status: " + wo.getStatus() + " | Beskrivning: " + wo.getDescription()));
                    }
                    break;
                case "4":
                    running = false;
            }
        }
    }

    private static boolean hasAccess(String currentRole, String... allowedRoles) {
        for (String role : allowedRoles) {
            if (role.equalsIgnoreCase(currentRole)) {
                return true;
            }
        }
        return false;
    }

}