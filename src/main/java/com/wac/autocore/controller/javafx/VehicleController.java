package com.wac.autocore.controller.javafx;

import com.wac.autocore.dto.customer.CustomerSummaryDto;
import com.wac.autocore.dto.vechicle.VehicleCreateDto;
import com.wac.autocore.dto.vechicle.VehicleSummaryDto;
import com.wac.autocore.exception.DatabaseException;
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
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.Objects;

@Controller
@Scope("prototype") // Lägg denna på controllers som styr flera fxml filer.
public class VehicleController extends OverController {

    @FXML private TableView<VehicleSummaryDto> vehicleTable;
    @FXML private TableColumn<VehicleSummaryDto, Integer> idColumn;
    @FXML private TableColumn<VehicleSummaryDto, String> registrationNumberColumn;
    @FXML private TableColumn<VehicleSummaryDto, String> brandColumn;
    @FXML private TableColumn<VehicleSummaryDto, String> modelColumn;
    @FXML private TableColumn<VehicleSummaryDto, Integer> yearColumn;
    @FXML private TableColumn<VehicleSummaryDto, String> customerColumn;

    @FXML private TextField registrationNumberField;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private ComboBox<CustomerSummaryDto> customerComboBox;

    private final VehicleService vehicleService;
    private final CustomerService customerService;

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

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
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomerName())
            );
            loadVehicleData();
            vehicleTable.requestFocus();
        }

        // NewVehicleView.fxml
        if (customerComboBox != null) {
            try {
                customerComboBox.setItems(FXCollections.observableArrayList(customerService.getAllCustomers()));

                customerComboBox.setConverter(new StringConverter<CustomerSummaryDto>() {

                    @Override
                    public String toString(CustomerSummaryDto customer) {
                        if (customer == null) {
                            return "";
                        }
                        return customer.getId() + " - " + customer.getName()
                                + " | " + getString("customer.phoneColumn") + ": "
                                + customer.getPhone()
                                + " | " + getString("customer.emailColumn") + ": "
                                + customer.getEmail()
                                + " | " + getString("customer.vipColumn") + ": "
                                + getString(customer.isVip() ? "common.yes" : "common.no");
                    }

                    @Override
                    public CustomerSummaryDto fromString(String text) {
                        return null;
                    }
                });
            } catch (AuthorizationDeniedException e) {
                String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
                logger.error(
                        "Method: initialize/changePrice | Authorization error : {} | User: {} ",
                        e.getMessage(),
                        username, e);

                if (messages != null) {
                    messages.showError(
                            getString("common.error.unauthorized"));
                }

            } catch (DatabaseException e) {
                if (messages != null) {
                    messages.showError(
                            getString(e.getMessage()));
                }

            } catch (Exception e) {

                logger.error(
                        "Method: initialize | Unexpected",
                        e);

                if (messages != null) {
                    messages.showError(
                            getString("common.error.unexpected"));
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

                ObservableList<VehicleSummaryDto> vehicleData =
                        FXCollections.observableArrayList(
                                vehicleService.getAllVehicles());

                vehicleTable.setItems(vehicleData);

            } catch (AuthorizationDeniedException e) {
                String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
                logger.error(
                        "Method: loadVehicleData | Authorization error : {} | User: {} ",
                        e.getMessage(),
                        username, e);

                if (messages != null) {
                    messages.showError(
                            getString("common.error.unauthorized"));
                }

            } catch (DatabaseException e) {
                if (messages != null) {
                    messages.showError(
                            getString(e.getMessage()));
                }

            } catch (Exception e) {
                logger.error(
                        "Method: loadVehicleData | Unexpected",
                        e);
                if (messages != null) {
                    messages.showError(
                            getString("common.error.unexpected"));
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
        loadCenterView("/com/wac/autocore/controller/view/NewVehicleView.fxml");
    }

    @FXML
    private void handleSaveVehicle() {
        if (registrationNumberField != null) {
            String regNo = registrationNumberField.getText();
            String brand = brandField.getText();
            String model = modelField.getText();
            CustomerSummaryDto selectedCustomer = customerComboBox != null ? customerComboBox.getValue() : null;

            try {
                int year = Integer.parseInt(yearField.getText().trim());

                if (regNo != null && !regNo.trim().isEmpty() && selectedCustomer != null) {

                    VehicleCreateDto createDto = new VehicleCreateDto(regNo.trim(), brand.trim(), model.trim(), year, selectedCustomer.getId());

                    vehicleService.saveVehicle(createDto);

                    messages.showSuccess(getString("vehicle.showsuccess.vehicle_created"));
                    navigateToVehicleView();
                } else {
                    messages.showError(getString("vehicle.error.enter_data"));
                }
            } catch (AuthorizationDeniedException e) {
                String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
                logger.error(
                        "Method: loadVehicleData | Authorization error : {} | User: {} ",
                        e.getMessage(),
                        username, e);

                messages.showError(
                        getString("common.error.unauthorized"));

            } catch (NumberFormatException e) {

                messages.showError(
                        getString("vehicle.error.incorrect_year"));

            } catch (Exception e) {

                logger.error(
                        "Method: handleSaveVehicle | Unexpected",
                        e);

                messages.showError(
                        getString("vehicle.error.save_vehicle"));
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
        loadCenterView("/com/wac/autocore/controller/view/VehicleView.fxml");
    }
}