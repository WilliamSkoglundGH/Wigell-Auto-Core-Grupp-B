package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Booking;

import com.wac.autocore.service.BookingService;
import com.wac.autocore.service.MechanicService;
import com.wac.autocore.service.VehicleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
@Controller
public class BookingController extends OverController {


    // TABLE VIEW (BookingView.fxml)
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Long> idColumn;
    @FXML private TableColumn<Booking, String> vehicleIdColumn;
    @FXML private TableColumn<Booking, LocalDate> dateColumn;
    @FXML private TableColumn<Booking, String> descriptionColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, String> mechanicColumn;




    // FORM FIELDS (NewBookingView.fxml)
    @FXML private ComboBox<String> vehicleIdField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionField;
    @FXML private Button saveButton;
    //@FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<String> mechanicField;

    private final BookingService bookingService;
    private final VehicleService vehicleService;
    private final MechanicService mechanicService;
    private final ApplicationContext applicationContext;


    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    public BookingController(BookingService bookingService, VehicleService vehicleService, MechanicService mechanicService, ApplicationContext applicationContext) {
        this.bookingService = bookingService;
        this.vehicleService = vehicleService;
        this.mechanicService = mechanicService;
        this.applicationContext = applicationContext;
    }

    // ---------------------------------------------------------
    // INITIALIZE
    // ---------------------------------------------------------
    @FXML
    public void initialize() {

        // BookingView.fxml
        if (bookingTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            vehicleIdColumn.setCellValueFactory( cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getVehicle().getId() + " - " + cellData.getValue().getVehicle().getRegistrationNumber()));
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            mechanicColumn.setCellValueFactory( cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMechanic().getId() + " - " +cellData.getValue().getMechanic().getName()));

            loadBookingData();
        }

        // NewBookingView.fxml
        if (vehicleIdField != null) {
            loadVehicleDropdown();
        }
        /*if (statusComboBox != null) {
            loadStatusDropdown();
        }*/
        if (mechanicField != null) {
            loadMechanicDropdown();
        }


        if (descriptionField != null) {
            descriptionField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.TAB) {
                    event.consume();
                    saveButton.requestFocus();
                }
            });
        }
    }

    private void loadBookingData() {
        if (bookingTable != null) {
            try {
                ObservableList<Booking> bookingData = FXCollections.observableArrayList(
                        bookingService.getAllBookings()
                );
                bookingTable.setItems(bookingData);
            } catch (Exception e) {
                logger.error("Could not load bookings from database. {}", e.getMessage(), e);
                if (messages != null) {
                    messages.showError("Could not load bookings from database.");
                }
            }
        }
    }

    // ---------------------------------------------------------
    // VEHICLE DROPDOWN
    // ---------------------------------------------------------
    private void loadVehicleDropdown() {
        List<String> vehicleList = vehicleService.getAllVehicles().stream()
                .map(v -> v.getId() + " - " +
                        v.getBrand() + " " + v.getModel() +
                        " (" + v.getRegistrationNumber() + ")")
                .collect(Collectors.toList());

        vehicleIdField.setItems(FXCollections.observableArrayList(vehicleList));
    }
    // ---------------------------------------------------------
    // MECHANIC DROPDOWN
    // ---------------------------------------------------------
    private void loadMechanicDropdown() {
        List<String> mechanicList = mechanicService.getAllMechanics().stream()
                .map(m -> m.getId() + " - " +
                        m.getName())
                .collect(Collectors.toList());

        mechanicField.setItems(FXCollections.observableArrayList(mechanicList));
    }

    // ---------------------------------------------------------
    // STATUS DROPDOWN
    // ---------------------------------------------------------
   /* private void loadStatusDropdown() {
        statusComboBox.setItems(FXCollections.observableArrayList(
                "BOOKED",
                "WORK_ORDER_CREATED",
                "IN_PROGRESS",
                "COMPLETED"
        ));
        statusComboBox.setValue("BOOKED");
    }*/

    // ---------------------------------------------------------
    // NEW BOOKING BUTTON
    // ---------------------------------------------------------
    @FXML
    private void handleNewBooking() {
        if (messages != null) {
            messages.clearMessage();
        }
        loadCenterView("/com/wac/autocore/gui/view/NewBookingView.fxml");
    }

    // ---------------------------------------------------------
    // SAVE BOOKING
    // ---------------------------------------------------------
    @FXML
    private void handleSaveBooking() {
        try {
            // Vehicle ID från ComboBox ("3 - Volvo XC90")
            String vehicleString = vehicleIdField.getValue();

            if (vehicleString == null) {
                messages.showError("Please select a vehicle.");
                return;
            }

            Long vehicleId = Long.parseLong(vehicleString.split(" - ")[0]);

            // Datum
            LocalDate selectedDate = datePicker.getValue();
            LocalDate today = LocalDate.now();

            if (selectedDate == null) {
                messages.showError("Please select a date.");
                return;
            }

            if (selectedDate.isBefore(today)) {
                messages.showError("The booking date cannot be before today.");
                return;
            }

            // Description
            String description = descriptionField.getText();

            // Mechanic ID från ComboBox ("1 - Olle Svensson")
            String mechanicString = mechanicField.getValue();

            if (mechanicString == null) {
                messages.showError("Please select a mechanic.");
                return;
            }

            Long mechanicId = Long.parseLong(mechanicString.split(" - ")[0]);

            // Lägg till i databasen
            bookingService.saveBooking(vehicleId, selectedDate, description, mechanicId);

            // Navigera tillbaka
            messages.showSuccess("Booking created.");
            navigateToBookingView();


        } catch (Exception e) {
            logger.error("Could not save booking to database. {}", e.getMessage(), e);
            messages.showError("An unexpected error occurred. Please check the booking list before trying again.");
        }
    }


    @FXML
    private void handleCancel() {
        messages.clearMessage();
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
            ResourceBundle bundle = ResourceBundle.getBundle("com.wac.autocore.gui.lang.messages");
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle); // Använder fxmlPath här!
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BookingController) {
                BookingController bc = (BookingController) controller;
                bc.setMessages(messages);

                BorderPane mainLayout = findMainLayout();
                if (mainLayout != null) {
                    mainLayout.setCenter(view);

                    if (bc.vehicleIdField != null) {
                        bc.vehicleIdField.requestFocus();
                    } else if (bc.bookingTable != null) {
                        bc.bookingTable.requestFocus();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (messages != null) {
                messages.showError("Could not open the booking view. Please try again.");
            }
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




}



