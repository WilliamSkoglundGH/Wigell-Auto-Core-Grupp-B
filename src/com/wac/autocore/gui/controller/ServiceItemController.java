package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.ServiceItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for the service item view. Fetches data directly from the database.
 */
public class ServiceItemController {

    @FXML
    private TableView<ServiceItem> serviceItemTable;

    @FXML
    private TableColumn<ServiceItem, Integer> idColumn;

    @FXML
    private TableColumn<ServiceItem, String> nameColumn;

    @FXML
    private TableColumn<ServiceItem, String> descriptionColumn;

    @FXML
    private TableColumn<ServiceItem, Double> priceColumn;

    @FXML
    private TableColumn<ServiceItem, Integer> estimatedMinutesColumn;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        estimatedMinutesColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedMinutes"));

        loadServiceItemData();
    }

    public void loadServiceItemData() {
        ObservableList<ServiceItem> serviceItemData = FXCollections.observableArrayList(
                Database.getServiceItems()
        );

        serviceItemTable.setItems(serviceItemData);
    }
}