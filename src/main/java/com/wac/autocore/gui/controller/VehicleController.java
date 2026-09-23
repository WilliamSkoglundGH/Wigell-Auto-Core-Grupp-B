package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Customer;
import com.wac.autocore.model.Vehicle;
import com.wac.autocore.service.CustomerService;
import com.wac.autocore.service.VehicleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class VehicleController extends OverController {

    @FXML
    private TableView<Vehicle> vehicleTable;
    @FXML
    private TableColumn<Vehicle, Integer> idColumn;
    @FXML
    private TableColumn<Vehicle, String> registrationNumberColumn;
    @FXML
    private TableColumn<Vehicle, String> brandColumn;
    @FXML
    private TableColumn<Vehicle, String> modelColumn;
    @FXML
    private TableColumn<Vehicle, Integer> yearColumn;
    @FXML
    private TableColumn<Vehicle, String> customerColumn;

    @FXML
    private TextField registrationNumberField;
    @FXML
    private TextField brandField;
    @FXML
    private TextField modelField;
    @FXML
    private TextField yearField;
    @FXML
    private ComboBox<Customer> customerComboBox;

    private UserMessages messages;
    private final VehicleService vehicleService;
    private final CustomerService customerService;

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    public VehicleController(VehicleService vehicleService, CustomerService customerService) {
        this.vehicleService = vehicleService;
        this.customerService = customerService;
    }

    @FXML
    public void initialize() {
        if (vehicleTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            registrationNumberColumn.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
            brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
            modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
            yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
            customerColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getName())
            );
            loadVehicleData();
        }
    }

    public void loadVehicleData() {
        if (vehicleTable != null) {
            try {
                ObservableList<Vehicle> vehicleData = FXCollections.observableArrayList(
                        vehicleService.getAllVehicles()
                );
                vehicleTable.setItems(vehicleData);
            } catch (Exception e) {
                logger.error("Could not load vehicles from database. {}", e.getMessage(), e);
                if (messages != null) {
                    messages.showError("Could not load vehicles from database.");
                }
            }
        }
    }

    @FXML
    private void handleNewVehicle() {
        Parent newVehicleView;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/NewVehicleView.fxml"));
            loader.setController(this);
            newVehicleView = loader.load();
        } catch (IOException e) {
            logger.error("Could not load NewVehicleView.fxml. {}", e.getMessage(), e);
            messages.showError("Could not load NewVehicleView.fxml.");
            return;
        }

        if (customerComboBox != null) {
            try {
                customerComboBox.setItems(FXCollections.observableArrayList(customerService.getAllCustomers()));
            } catch (Exception e) {
                logger.error("Could not get all customers: {}", e.getMessage(), e);
                messages.showError("Could not get all customers.");
            }
        }

        BorderPane mainLayout = findMainLayout();
        if (mainLayout != null) {
            mainLayout.setCenter(newVehicleView);
            if (registrationNumberField != null) {
                registrationNumberField.requestFocus();
            }
        } else {
            logger.error("Could not find BorderPane to show NewVehicleView.");
            messages.showError("Could not find BorderPane!");
        }
    }

    @FXML
    private void handleSaveVehicle() {
        if (registrationNumberField != null) {
            String regNo = registrationNumberField.getText();
            String brand = brandField.getText();
            String model = modelField.getText();
            Customer selectedCustomer = customerComboBox != null ? customerComboBox.getValue() : null;

            try {
                int year = Integer.parseInt(yearField.getText().trim());

                if (regNo != null && !regNo.trim().isEmpty() && selectedCustomer != null) {
                    Customer customer = customerService.getCustomer(selectedCustomer.getId());
                    Vehicle newVehicle = new Vehicle(regNo, brand.trim(), model.trim(), year, customer);
                    vehicleService.saveVehicle(newVehicle);
                    messages.showSuccess("Vehicle created.");
                    navigateToVehicleView();
                } else {
                    logger.error("Could not save Vehicle, data missing.");
                    messages.showError("Please enter all data and choose a customer!");
                }
            } catch (NumberFormatException e) {
                logger.error("Could not save vehicle: {}", e.getMessage(), e);
                messages.showError("Not a correct year!");
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
            VehicleController controller = loader.getController();
            controller.setMessages(messages);

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(vehicleView);
                controller.vehicleTable.requestFocus();
            } else {
                logger.error("Could not find BorderPane to show VehicleView.");
                messages.showError("Could not find BorderPane!");
            }
        } catch (IOException e) {
            logger.error("Could not load VehicleView.fxml: {}", e.getMessage(), e);
            messages.showError("Could not load VehicleView.fxml");
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
    public void setMessages(UserMessages messages) {
        this.messages = messages;
    }

}