package com.wac.autocore;
import com.wac.autocore.dto.customer.CustomerCreateDto;
import com.wac.autocore.repository.CustomerRepository;
import com.wac.autocore.repository.JdbcCustomerRepository;
import com.wac.autocore.service.CustomerService;

import java.sql.Connection;
import java.sql.DriverManager;

public class StandaloneApp {
    public static void main(String[] args) {
        System.out.println("--- Startar fristående Java-applikation mot MySQL (UTAN Spring) ---");

        try {
            // 1. Ladda MySQL-drivern explicit
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Anslut till din riktiga MySQL-databas (byt ut 'autocore_db', användarnamn och lösenord)
            String url = "jdbc:mysql://localhost:3306/wigell_autocore_db?useSSL=false&serverTimezone=UTC";
            Connection connection = DriverManager.getConnection(url, "root", "root");

            // 3. Skapa JDBC-repositoryt manuellt
            CustomerRepository customerRepo = new JdbcCustomerRepository(connection);

            // 4. Skicka in repositoryt i den GEMENSAMMA servicen med 'new'
            CustomerService customerService = new CustomerService(customerRepo);

            // 5. Kör operationen (exakt samma affärsregler som i Spring-appen)
            CustomerCreateDto dto = new CustomerCreateDto("Anna Andersson", "0701234567", "anna@wigell.se", true);
            customerService.createCustomer(dto);

            System.out.println("Kund sparad framgångsrikt i MySQL via fristående program!");

        } catch (Exception e) {
            System.err.println("Ett fel inträffade: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("--- Programmet avslutat ---");
    }
}