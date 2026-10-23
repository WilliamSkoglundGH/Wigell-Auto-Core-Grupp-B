package com.wac.autocore.repository;

import com.wac.autocore.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCustomerRepository implements CustomerRepository {
    private final Connection connection;

    public JdbcCustomerRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Customer save(Customer customer) {
        // Om kunden saknar ID gör vi INSERT, annars UPDATE (eller enklare för demo: INSERT)
        String sql = "INSERT INTO customer (name, phone, email, vip) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setBoolean(4, customer.isVip());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                // Sätt genererat ID på kund-objektet (om man vill)
                Long generatedId = rs.getLong(1);
                // (Notera: ID är som standard final/saknar setter i din modell,
                //  men du kan lägga till en protected/public setId om det behövs!)
            }
            return customer;
        } catch (SQLException e) {
            throw new RuntimeException("Database error in JDBC save", e);
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        String sql = "SELECT id, name, phone, email, vip FROM customer WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Customer c = new Customer(rs.getString("name"), rs.getString("phone"), rs.getString("email"));
                c.setVip(rs.getBoolean("vip"));
                return Optional.of(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in JDBC findById", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id, name, phone, email, vip FROM customer";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Customer c = new Customer(rs.getString("name"), rs.getString("phone"), rs.getString("email"));
                c.setVip(rs.getBoolean("vip"));
                list.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in JDBC findAll", e);
        }
        return list;
    }
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM customer";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in JDBC count", e);
        }
        return 0;
    }
}