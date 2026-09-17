package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.Booking;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BookingController {

    // TABLE VIEW (BookingView.fxml)
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Integer> idColumn;
    @FXML private TableColumn<Booking, Integer> vehicleIdColumn;
    @FXML private TableColumn<Booking, LocalDate> dateColumn;
    @FXML private TableColumn<Booking, String> descriptionColumn;
    @FXML private TableColumn<Booking, String> statusColumn;

    // FORM FIELDS (NewBookingView.fxml)
    @FXML private ComboBox<String> vehicleIdField;
    @FXML private DatePicker datePicker;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<String> statusComboBox;

    // ---------------------------------------------------------
    // INITIALIZE
    // ---------------------------------------------------------
    @FXML
    public void initialize() {

        // BookingView.fxml
        if (bookingTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            vehicleIdColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            loadBookingData();
        }

        // NewBookingView.fxml
        if (vehicleIdField != null) {
            loadVehicleDropdown();
        }
        if (statusComboBox != null) {
            loadStatusDropdown();
        }
    }

    private void loadBookingData() {
        ObservableList<Booking> bookingData =
                FXCollections.observableArrayList(Database.getBookings());
        bookingTable.setItems(bookingData);
    }

    // ---------------------------------------------------------
    // VEHICLE DROPDOWN
    // ---------------------------------------------------------
    private void loadVehicleDropdown() {
        List<String> vehicleList = Database.getVehicles().stream()
                .map(v -> v.getId() + " - " +
                        v.getBrand() + " " + v.getModel() +
                        " (" + v.getRegistrationNumber() + ")")
                .collect(Collectors.toList());

        vehicleIdField.setItems(FXCollections.observableArrayList(vehicleList));
    }

    // ---------------------------------------------------------
    // STATUS DROPDOWN
    // ---------------------------------------------------------
    private void loadStatusDropdown() {
        statusComboBox.setItems(FXCollections.observableArrayList(
                "BOOKED",
                "CANCELLED",
                "COMPLETED",
                "IN_PROGRESS"
        ));
        statusComboBox.setValue("BOOKED");
    }

    // ---------------------------------------------------------
    // NEW BOOKING BUTTON
    // ---------------------------------------------------------
    @FXML
    private void handleNewBooking() {
        loadCenterView("/com/wac/autocore/gui/view/NewBookingView.fxml");
    }

    // ---------------------------------------------------------
    // SAVE BOOKING
    // ---------------------------------------------------------
    @FXML
    private void handleSaveBooking() {
        try {
            String vehicleString = vehicleIdField.getValue();
            int vehicleId = Integer.parseInt(vehicleString.split(" - ")[0]);

            LocalDate date = datePicker.getValue();
            String description = descriptionField.getText();
            String status = statusComboBox.getValue();

            int newId = Database.getBookings().size() + 1;

            Booking booking = new Booking(newId, vehicleId, date, description);
            booking.setStatus(status);

            Database.getBookings().add(booking);

            navigateToBookingView();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        navigateToBookingView();
    }

    // ---------------------------------------------------------
    // RETURN TO BOOKING TABLE VIEW
    // ---------------------------------------------------------
    private void navigateToBookingView() {
        loadCenterView("/com/wac/autocore/gui/view/BookingView.fxml");
    }

    // ---------------------------------------------------------
    // GENERIC VIEW LOADER
    // ---------------------------------------------------------
    private void loadCenterView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(view);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------
    // FIND MAIN BORDERPANE
    // ---------------------------------------------------------
    private BorderPane findMainLayout() {
        Scene scene = null;

        if (vehicleIdField != null && vehicleIdField.getScene() != null) {
            scene = vehicleIdField.getScene();
        } else if (bookingTable != null && bookingTable.getScene() != null) {
            scene = bookingTable.getScene();
        }

        if (scene != null && scene.getRoot() instanceof BorderPane) {
            return (BorderPane) scene.getRoot();
        }

        if (scene != null) {
            return searchBorderPaneRecursive(scene.getRoot());
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
                if (found != null) return found;
            }
        }
        return null;
    }
}



