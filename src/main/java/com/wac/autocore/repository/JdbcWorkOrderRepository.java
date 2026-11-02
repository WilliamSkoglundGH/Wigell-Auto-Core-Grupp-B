package com.wac.autocore.repository;

import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.model.enums.WorkOrderState;
import com.wac.autocore.model.enums.WorkOrderType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcWorkOrderRepository implements WorkOrderRepository {
    private final Connection connection;

    public JdbcWorkOrderRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<WorkOrder> findAll() {
        List<WorkOrder> list = new ArrayList<>();
        String sql = "SELECT id, description, status, work_order_type, start_time, end_time FROM work_order";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                WorkOrder w = mapResultSetToWorkOrder(rs);
                list.add(w);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in JDBC findAll work orders", e);
        }
        return list;
    }

    @Override
    public Optional<WorkOrder> findById(Long id) {
        String sql = "SELECT id, description, status, work_order_type, start_time, end_time FROM work_order WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                WorkOrder w = mapResultSetToWorkOrder(rs);
                return Optional.of(w);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in JDBC findById work order", e);
        }
        return Optional.empty();
    }

    @Override
    public WorkOrder save(WorkOrder workOrder) {
        // Om den har ID gör vi en UPDATE
        if (workOrder.getId() != null) {
            String sql = "UPDATE work_order SET status = ?, start_time = ?, end_time = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, workOrder.getStatus().name());

                // Hantera LocalDateTime som kan vara null
                stmt.setTimestamp(2, workOrder.getStartTime() != null ? Timestamp.valueOf(workOrder.getStartTime()) : null);
                stmt.setTimestamp(3, workOrder.getEndTime() != null ? Timestamp.valueOf(workOrder.getEndTime()) : null);
                stmt.setLong(4, workOrder.getId());

                stmt.executeUpdate();
                return workOrder;
            } catch (SQLException e) {
                throw new RuntimeException("Database error in JDBC update work order", e);
            }
        } else {
            String sql = "INSERT INTO work_order (description, status, work_order_type) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, workOrder.getDescription());
                stmt.setString(2, workOrder.getStatus().name());
                stmt.setString(3, workOrder.getType().name());
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    workOrder.setId(rs.getLong(1));
                }
                return workOrder;
            } catch (SQLException e) {
                throw new RuntimeException("Database error in JDBC insert work order", e);
            }
        }
    }

    // Hjälpmetod för att mappa ResultSet till WorkOrder-objektet
    private WorkOrder mapResultSetToWorkOrder(ResultSet rs) throws SQLException {
        WorkOrderType type = WorkOrderType.valueOf(rs.getString("work_order_type"));
        WorkOrder w = new WorkOrder(type);
        w.setId(rs.getLong("id"));
        w.setDescription(rs.getString("description"));
        w.setStatus(WorkOrderState.valueOf(rs.getString("status")));

        Timestamp start = rs.getTimestamp("start_time");
        if (start != null) {
            w.setStartTime(start.toLocalDateTime());
        }

        Timestamp end = rs.getTimestamp("end_time");
        if (end != null) {
            w.setEndTime(end.toLocalDateTime());
        }

        return w;
    }

    public List<WorkOrder> findByMechanic_Id(Long mechanicId) {
        return null;
    }

    public Optional<WorkOrder> findByBooking_Id(Long bookingId) {
        return null;
    }
}