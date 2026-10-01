package com.wac.autocore.gui.controller;

import com.wac.autocore.dto.workorder.WorkOrderForMechanicDto;
import com.wac.autocore.dto.workorder.WorkOrderSummaryDto;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.Vehicle;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.service.MechanicService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@Scope("prototype")
public class MechanicController extends OverController {

    private final MechanicService mechanicService;

    private static final Logger logger = LoggerFactory.getLogger(MechanicController.class);

    public MechanicController(MechanicService mechanicService, ApplicationContext applicationContext) {
        this.mechanicService = mechanicService;
        this.applicationContext = applicationContext;
    }

    @FXML
    private TableView<Mechanic> mechanicTable;
    @FXML
    private TableColumn<Mechanic, Long> idColumn;
    @FXML
    private TableColumn<Mechanic, String> nameColumn;
    @FXML
    private TableColumn<Mechanic, String> phoneColumn;
    @FXML
    private TableColumn<Mechanic, String> specializationColumn;

    // Label så man ser vilken mekanikers bokningar man är inne på
    @FXML private Label headerLabel;

    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Long> bookingIdColumn;
    @FXML private TableColumn<Booking, String> vehicleColumn;
    @FXML private TableColumn<Booking, String> dateColumn;
    @FXML private TableColumn<Booking, String> descriptionColumn;
    @FXML private TableColumn<Booking, String> statusColumn;

    @FXML private TableView<WorkOrderForMechanicDto> workorderTable;
    @FXML private TableColumn<WorkOrderForMechanicDto, Long> woIdColumn;
    @FXML private TableColumn<WorkOrderForMechanicDto, String> woBookingIdColumn;
    @FXML private TableColumn<WorkOrderForMechanicDto, String> woVehicleColumn;
    @FXML private TableColumn<WorkOrderForMechanicDto, String> woStatusColumn;
    @FXML private TableColumn<WorkOrderForMechanicDto, String> woStartTimeColumn;
    @FXML private TableColumn<WorkOrderForMechanicDto, String> woEndTimeColumn;
    @FXML private TableColumn<WorkOrderForMechanicDto, String> woDescriptionColumn;

    // Fält för visning i MechanicView av kundens namn
    @FXML private TableColumn<Booking, String> customerColumn;
    @FXML private TableColumn<WorkOrder, String> woCustomerColumn;


    @FXML
    public void initialize() {

        if (mechanicTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
            specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));

            loadMechanicData();
            mechanicTable.requestFocus();
        }

        // När bookings-vyn är laddad kommer de här fälten vara != null
        if (bookingTable != null) {

            bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            // Vehicle = endast brand + model
            vehicleColumn.setCellValueFactory(cellData -> {
                Booking b = cellData.getValue();

                if (b.getVehicle() != null) {
                    Vehicle v = b.getVehicle();
                    return new javafx.beans.property.SimpleStringProperty(
                            v.getBrand() + " " + v.getModel()
                    );
                }

                return new javafx.beans.property.SimpleStringProperty("");
            });

            // Customer = hämtas via vehicle.getCustomer()
            customerColumn.setCellValueFactory(cellData -> {
                Booking b = cellData.getValue();

                if (b.getVehicle() != null && b.getVehicle().getCustomer() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            b.getVehicle().getCustomer().getName()
                    );
                }

                return new javafx.beans.property.SimpleStringProperty("");
            });

            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            statusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            getString("status." + cellData.getValue().getStatus())
                    )
            );
        }

        if (workorderTable != null) {

            woIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            woVehicleColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
            woDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            woBookingIdColumn.setCellValueFactory(cellData -> {
                WorkOrderForMechanicDto dto = cellData.getValue();
                if (dto != null && dto.getBookingId() != null) {
                    return new SimpleStringProperty(String.valueOf(dto.getBookingId()));
                }
                return new SimpleStringProperty("");
            });

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            woStartTimeColumn.setCellValueFactory(cellData -> {
                WorkOrderForMechanicDto dto = cellData.getValue();
                if (dto != null && dto.getStartTime() != null) {
                    return new SimpleStringProperty(dto.getStartTime().format(formatter));
                }
                return new SimpleStringProperty("");
            });

            woEndTimeColumn.setCellValueFactory(cellData -> {
                WorkOrderForMechanicDto dto = cellData.getValue();
                if (dto != null && dto.getEndTime() != null) {
                    return new SimpleStringProperty(dto.getEndTime().format(formatter));
                }
                return new SimpleStringProperty("");
            });

            woStatusColumn.setCellValueFactory(cellData -> {
                WorkOrderForMechanicDto dto = cellData.getValue();
                if (dto != null && dto.getStatus() != null) {
                    return new SimpleStringProperty(
                            getString( dto.getStatus().name().toLowerCase())
                    );
                }
                return new SimpleStringProperty("");
            });
        }
    }


    public void loadMechanicData() {
        if (mechanicTable != null) {
            try {
                ObservableList<Mechanic> mechanicData = FXCollections.observableArrayList(
                        mechanicService.getAllMechanics()
                );
                mechanicTable.setItems(mechanicData);
            } catch (Exception e) {
                logger.error("Could not load mechanics from database. {}", e.getMessage(), e);
                if (messages != null) {
                    messages.showError(getString("mechanic.error.could_not_load"));
                }
            }
        }
    }

    @FXML
    private void handleBookings() {

        if (messages != null) {
            messages.clearMessage();
        }

        Mechanic selected = mechanicTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messages.showError(getString("mechanic.error.select_mechanic"));
            return;
        }

        // Få rätt controller-instans
        MechanicController controller =
                loadCenterView("/com/wac/autocore/gui/view/MechanicBookingsView.fxml");

        // Sätt in mekanikers namn
        controller.headerLabel.setText(getString("mechanic.bookingsFor") + ": " + selected.getName());

        // Hämta bokningar
        List<Booking> bookings = mechanicService.getBookingsForMechanic(selected.getId());

        // Fyll tabellen i rätt controller-instans
        controller.bookingTable.setItems(FXCollections.observableArrayList(bookings));
    }


    @FXML
    private void handleWorkorders() {

        if (messages != null) {
            messages.clearMessage();
        }

        Mechanic selected = mechanicTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messages.showError(getString("mechanic.error.select_mechanic"));
            return;
        }

        // Ladda den nya vyn och få rätt controller-instans
        MechanicController controller =
                loadCenterView("/com/wac/autocore/gui/view/MechanicWorkordersView.fxml");

        // vald mekaniker
        controller.headerLabel.setText(getString("mechanic.workordersFor") + ": " + selected.getName());

        // Hämta workorders för vald mekaniker
        List<WorkOrderForMechanicDto> workorders =
                mechanicService.getWorkOrdersForMechanic(selected.getId());

        // Fyll tabellen i rätt controller-instans
        controller.workorderTable.setItems(FXCollections.observableArrayList(workorders));
    }
    // Backa till alla mekanikerView
    @FXML
    private void handleBackToMechanics() {
        loadCenterView("/com/wac/autocore/gui/view/MechanicView.fxml");
    }



}





