package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.Vehicle;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.service.MechanicService;
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

    @FXML private TableView<WorkOrder> workorderTable;
    @FXML private TableColumn<WorkOrder, Long> woIdColumn;
    @FXML private TableColumn<WorkOrder, String> woVehicleColumn;
    @FXML private TableColumn<WorkOrder, String> bookingDateColumn;
    @FXML private TableColumn<WorkOrder, String> woDescriptionColumn;
    @FXML private TableColumn<WorkOrder, String> woStatusColumn;

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
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        }


        if (workorderTable != null) {

            woIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            // Vehicle = endast brand + model
            woVehicleColumn.setCellValueFactory(cellData -> {
                WorkOrder wo = cellData.getValue();

                if (wo.getBooking() != null && wo.getBooking().getVehicle() != null) {
                    Vehicle v = wo.getBooking().getVehicle();
                    return new javafx.beans.property.SimpleStringProperty(
                            v.getBrand() + " " + v.getModel()
                    );
                }

                return new javafx.beans.property.SimpleStringProperty("");
            });

            // Customer = hämtas via booking.vehicle.customer
            woCustomerColumn.setCellValueFactory(cellData -> {
                WorkOrder wo = cellData.getValue();

                if (wo.getBooking() != null &&
                        wo.getBooking().getVehicle() != null &&
                        wo.getBooking().getVehicle().getCustomer() != null) {

                    return new javafx.beans.property.SimpleStringProperty(
                            wo.getBooking().getVehicle().getCustomer().getName()
                    );
                }

                return new javafx.beans.property.SimpleStringProperty("");
            });

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            bookingDateColumn.setCellValueFactory(cellData -> {
                WorkOrder wo = cellData.getValue();

                if (wo.getBooking() != null && wo.getBooking().getDate() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            wo.getBooking().getDate().format(formatter)
                    );
                }

                return new javafx.beans.property.SimpleStringProperty("");
            });

            woDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            woStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
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
            messages.showError("Please select a mechanic first.");
            return;
        }

        // Få rätt controller-instans
        MechanicController controller =
                loadCenterView("/com/wac/autocore/gui/view/MechanicBookingsView.fxml");

        // Sätt in mekanikers namn
        controller.headerLabel.setText("Bookings for: " + selected.getName());

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
            messages.showError("Please select a mechanic first.");
            return;
        }

        // Ladda den nya vyn och få rätt controller-instans
        MechanicController controller =
                loadCenterView("/com/wac/autocore/gui/view/MechanicWorkordersView.fxml");

        // vald mekaniker
        controller.headerLabel.setText("Workorders for: " + selected.getName());

        // Hämta workorders för vald mekaniker
        List<WorkOrder> workorders =
                mechanicService.getWorkOrdersForMechanic(selected.getId());

        // Fyll tabellen i rätt controller-instans
        controller.workorderTable.setItems(FXCollections.observableArrayList(workorders));
    }



}





