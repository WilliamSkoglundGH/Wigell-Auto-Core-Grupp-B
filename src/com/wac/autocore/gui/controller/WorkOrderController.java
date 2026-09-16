package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.WorkOrder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controller for the work order view. Fetches data directly from the database.
 */
public class WorkOrderController {

    @FXML
    private TableView<WorkOrder> workOrderTable;

    @FXML
    private TableColumn<WorkOrder, Integer> idColumn;

    @FXML
    private TableColumn<WorkOrder, Integer> bookingIdColumn;

    @FXML
    private TableColumn<WorkOrder, Integer> mechanicIdColumn;

    @FXML
    private TableColumn<WorkOrder, List<Integer>> serviceItemIdsColumn;

    @FXML
    private TableColumn<WorkOrder, String> statusColumn;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        mechanicIdColumn.setCellValueFactory(new PropertyValueFactory<>("mechanicId"));
        serviceItemIdsColumn.setCellValueFactory(new PropertyValueFactory<>("serviceItemIds"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadWorkOrderData();
    }

    public void loadWorkOrderData() {
        ObservableList<WorkOrder> workOrderData = FXCollections.observableArrayList(
                Database.getWorkOrders()
        );

        workOrderTable.setItems(workOrderData);
    }
}