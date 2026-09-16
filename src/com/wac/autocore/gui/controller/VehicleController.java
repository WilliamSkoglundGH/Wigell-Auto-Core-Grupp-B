package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class VehicleController {

    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, Integer> idColumn;
    @FXML private TableColumn<Vehicle, String> registrationNumberColumn;
    @FXML private TableColumn<Vehicle, String> brandColumn;
    @FXML private TableColumn<Vehicle, String> modelColumn;
    @FXML private TableColumn<Vehicle, Integer> yearColumn;
    @FXML private TableColumn<Vehicle, Integer> customerIdColumn;

    @FXML private TextField registrationNumberField;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private TextField customerIdField;

    @FXML
    public void initialize() {
        if (vehicleTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            registrationNumberColumn.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
            brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
            modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
            yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
            customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));

            loadVehicleData();
        }
    }

    public void loadVehicleData() {
        if (vehicleTable != null) {
            ObservableList<Vehicle> vehicleData = FXCollections.observableArrayList(
                    Database.getVehicles()
            );
            vehicleTable.setItems(vehicleData);
        }
    }

    @FXML
    private void handleNewVehicle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/NewVehicleView.fxml"));
            loader.setController(this);
            Parent newVehicleView = loader.load();

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(newVehicleView);
            } else {
                System.err.println("Kunde inte hitta BorderPane för att visa fordonformuläret!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Kunde inte ladda NewVehicleView.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveVehicle() {
        if (registrationNumberField != null) {
            String regNo = registrationNumberField.getText();
            String brand = brandField.getText();
            String model = modelField.getText();

            try {
                int year = Integer.parseInt(yearField.getText().trim());
                int customerId = Integer.parseInt(customerIdField.getText().trim());

                if (regNo != null && !regNo.trim().isEmpty()) {
                    int newId = Database.getVehicles().size() + 1;

                    Vehicle newVehicle = new Vehicle(newId, regNo, brand, model, year, customerId);
                    Database.getVehicles().add(newVehicle);

                    navigateToVehicleView();
                }
            } catch (NumberFormatException e) {
                System.err.println("Year och Customer ID måste vara giltiga heltal!");
            }
        }
    }

    @FXML
    private void handleCancel() {
        navigateToVehicleView();
    }

    private void navigateToVehicleView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/VehicleView.fxml"));
            Parent vehicleView = loader.load();

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(vehicleView);
            } else {
                System.err.println("Kunde inte hitta BorderPane för att återgå till fordonstabellen!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Kunde inte ladda tillbaka VehicleView.fxml: " + e.getMessage());
        }
    }

    private BorderPane findMainLayout() {
        Scene scene = null;

        if (registrationNumberField != null && registrationNumberField.getScene() != null) {
            scene = registrationNumberField.getScene();
        } else if (vehicleTable != null && vehicleTable.getScene() != null) {
            scene = vehicleTable.getScene();
        }

        if (scene != null && scene.getRoot() != null) {
            Parent root = scene.getRoot();
            if (root instanceof BorderPane) {
                return (BorderPane) root;
            }
            return searchBorderPaneRecursive(root);
        }
        return null;
    }

    private BorderPane searchBorderPaneRecursive(Parent parent) {
        if (parent instanceof BorderPane) {
            return (BorderPane) parent;
        }
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                BorderPane found = searchBorderPaneRecursive((Parent) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}