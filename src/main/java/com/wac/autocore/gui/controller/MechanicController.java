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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class MechanicController extends OverController {

    private final MechanicService mechanicService;

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

    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Long> bookingIdColumn;
    @FXML private TableColumn<Booking, String> vehicleColumn;
    @FXML private TableColumn<Booking, String> dateColumn;
    @FXML private TableColumn<Booking, String> descriptionColumn;
    @FXML private TableColumn<Booking, String> statusColumn;

    @FXML private TableView<WorkOrder> workorderTable;
    @FXML private TableColumn<WorkOrder, Long> woIdColumn;
    @FXML private TableColumn<WorkOrder, String> woVehicleColumn;
    @FXML private TableColumn<WorkOrder, String> woDateColumn;
    @FXML private TableColumn<WorkOrder, String> woDescriptionColumn;
    @FXML private TableColumn<WorkOrder, String> woStatusColumn;



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

            vehicleColumn.setCellValueFactory(cellData -> {
                Booking b = cellData.getValue();

                String vehicleText = "";

                if (b.getVehicle() != null) {
                    vehicleText += b.getVehicle().getBrand() + " " + b.getVehicle().getModel();
                }

                if (b.getVehicle() != null && b.getVehicle().getCustomer() != null) {
                    vehicleText += " | " + b.getVehicle().getCustomer().getName();
                }

                return new javafx.beans.property.SimpleStringProperty(vehicleText);
            });

            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        if (workorderTable != null) {

            woIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            woVehicleColumn.setCellValueFactory(cellData -> {
                WorkOrder wo = cellData.getValue();
                String txt = "";

                if (wo.getBooking() != null && wo.getBooking().getVehicle() != null) {
                    Vehicle v = wo.getBooking().getVehicle();
                    txt += v.getBrand() + " " + v.getModel();

                    if (v.getCustomer() != null) {
                        txt += " | " + v.getCustomer().getName();
                    }
                }

                return new javafx.beans.property.SimpleStringProperty(txt);
            });

            woDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            woDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            woStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        }


    }


    public void loadMechanicData() {
        if (mechanicTable != null) {
            ObservableList<Mechanic> mechanicData =
                    FXCollections.observableArrayList(mechanicService.getAllMechanics());
            mechanicTable.setItems(mechanicData);
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

        // Ladda bokningsvyn – samma controller används
        loadCenterView("/com/wac/autocore/gui/view/MechanicBookingsView.fxml");

        // Hämta bokningar
        List<Booking> bookings = mechanicService.getBookingsForMechanic(selected.getId());

        // Fyll tabellen – fälten är redan injicerade via FXML
        bookingTable.setItems(FXCollections.observableArrayList(bookings));
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


        loadCenterView("/com/wac/autocore/gui/view/WorkorderView.fxml");

        List<WorkOrder> workorders = mechanicService.getWorkOrdersForMechanic(selected.getId());

        workorderTable.setItems(FXCollections.observableArrayList(workorders));
    }

}





