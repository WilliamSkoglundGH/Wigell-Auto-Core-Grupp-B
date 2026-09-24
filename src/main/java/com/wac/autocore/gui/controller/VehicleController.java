package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Customer;
import com.wac.autocore.model.Vehicle;
import com.wac.autocore.service.CustomerService;
import com.wac.autocore.service.VehicleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleController extends OverController {

    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, Integer> idColumn;
    @FXML private TableColumn<Vehicle, String> registrationNumberColumn;
    @FXML private TableColumn<Vehicle, String> brandColumn;
    @FXML private TableColumn<Vehicle, String> modelColumn;
    @FXML private TableColumn<Vehicle, Integer> yearColumn;
    @FXML private TableColumn<Vehicle, String> customerColumn;

    @FXML private TextField registrationNumberField;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private ComboBox<Customer> customerComboBox;

    private final VehicleService vehicleService;
    private final CustomerService customerService;

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    // Spring injicerar tjänster samt ApplicationContext
    public VehicleController(VehicleService vehicleService, CustomerService customerService, ApplicationContext applicationContext) {
        this.vehicleService = vehicleService;
        this.customerService = customerService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        // VehicleView.fxml
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
            vehicleTable.requestFocus();
        }

        // NewVehicleView.fxml
        if (customerComboBox != null) {
            try {
                customerComboBox.setItems(FXCollections.observableArrayList(customerService.getAllCustomers()));
            } catch (Exception e) {
                logger.error("Could not get all customers: {}", e.getMessage(), e);
                if (messages != null) {
                    messages.showError("Could not get all customers.");
                }
            }
        }

        if (registrationNumberField != null) {
            registrationNumberField.requestFocus();
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
        if (messages != null) {
            messages.clearMessage();
        }
        // Använder OverControllers gemensamma vyladdare
        loadCenterView("/com/wac/autocore/gui/view/NewVehicleView.fxml");
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
                    messages.showSuccess(getString("vehicle.showsuccess.vehicle_created"));
                    navigateToVehicleView();
                } else {
                    logger.error("Could not save Vehicle, data missing.");
                    messages.showError(getString("vehicle.error.enter_data"));
                }
            } catch (NumberFormatException e) {
                logger.error("Could not save vehicle: {}", e.getMessage(), e);
                messages.showError(getString("vehicle.error.incorrect_year"));
            }
        }
    }

    @FXML
    private void handleCancel() {
        if (messages != null) {
            messages.clearMessage();
        }
        navigateToVehicleView();
    }

    private void navigateToVehicleView() {
        loadCenterView("/com/wac/autocore/gui/view/VehicleView.fxml");
    }
}