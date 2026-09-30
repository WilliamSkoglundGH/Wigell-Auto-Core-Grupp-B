package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Booking;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.service.BookingService;
import com.wac.autocore.service.MechanicService;
import com.wac.autocore.service.ServiceItemService;
import com.wac.autocore.service.VehicleService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Scope("prototype")
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
    @FXML private ComboBox<String> mechanicField;
    @FXML private ListView<ServiceItem> servicesListView;

    private final BookingService bookingService;
    private final VehicleService vehicleService;
    private final MechanicService mechanicService;
    private final ServiceItemService serviceItemService;
    private final Map<Long, BooleanProperty> serviceSelections = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    public BookingController(BookingService bookingService, VehicleService vehicleService,
                             MechanicService mechanicService, ApplicationContext applicationContext,
                             ServiceItemService serviceItemService) {
        this.bookingService = bookingService;
        this.vehicleService = vehicleService;
        this.mechanicService = mechanicService;
        this.applicationContext = applicationContext;
        this.serviceItemService = serviceItemService;
    }

    // ---------------------------------------------------------
    // INITIALIZE
    // ---------------------------------------------------------
    @FXML
    public void initialize() {
        // BookingView.fxml
        if (bookingTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            vehicleIdColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getVehicle().getId() + " - " + cellData.getValue().getVehicle().getRegistrationNumber()));
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            statusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            getString("status." + cellData.getValue().getStatus())
                    )
            );
            mechanicColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMechanic().getId() + " - " + cellData.getValue().getMechanic().getName()));

            loadBookingData();
            bookingTable.requestFocus();
        }

        // NewBookingView.fxml
        if (vehicleIdField != null) {
            loadVehicleDropdown();
            vehicleIdField.requestFocus();
        }
        if (mechanicField != null) {
            loadMechanicDropdown();
        }

        loadServiceList();

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
                    messages.showError(getString("booking.error.could_not_load"));
                }
            }
        }
    }

    private void loadVehicleDropdown() {
        List<String> vehicleList = vehicleService.getAllVehicles().stream()
                .map(v -> v.getId() + " - " +
                        v.getBrand() + " " + v.getModel() +
                        " (" + v.getRegistrationNumber() + ")")
                .collect(Collectors.toList());

        vehicleIdField.setItems(FXCollections.observableArrayList(vehicleList));
    }

    private void loadServiceList() {
        if (servicesListView != null) {
            ObservableList<ServiceItem> serviceItems =
                    FXCollections.observableArrayList(serviceItemService.getAllServiceItems());

            for (ServiceItem item : serviceItems) {
                serviceSelections.putIfAbsent(item.getId(),
                        new SimpleBooleanProperty(false));
            }

            servicesListView.setCellFactory(CheckBoxListCell.forListView(
                    item -> serviceSelections.get(item.getId()),
                    new StringConverter<ServiceItem>() {
                        @Override
                        public String toString(ServiceItem item) {

                            String text = item.getName() + " — " + item.getPrice() + " SEK"
                                    + " — " + item.getEstimatedMinutes() + " min";

                            String description = item.getDescription();

                            if (description != null && !description.trim().isEmpty()) {
                                text += "\n" + description;
                            }
                            return text;
                        }

                        @Override
                        public ServiceItem fromString(String text) {
                            return null;
                        }
                    }
            ));

            servicesListView.setItems(serviceItems);
        }
    }


    private void loadMechanicDropdown() {
        List<String> mechanicList = mechanicService.getAllMechanics().stream()
                .map(m -> m.getId() + " - " + m.getName()
                        + " | " + getString("mechanic.specializationColumn")
                        + ": " + m.getSpecialization())
                .collect(Collectors.toList());

        mechanicField.setItems(FXCollections.observableArrayList(mechanicList));
    }

    // ---------------------------------------------------------
    // BUTTON ACTIONS & NAVIGATION
    // ---------------------------------------------------------
    @FXML
    private void handleNewBooking() {
        if (messages != null) {
            messages.clearMessage();
        }
        loadCenterView("/com/wac/autocore/gui/view/NewBookingView.fxml");
    }

    @FXML
    private void handleSaveBooking() {
        try {
            String vehicleString = vehicleIdField.getValue();
            if (vehicleString == null) {
                messages.showError(getString("booking.error.select_vehicle"));
                return;
            }
            Long vehicleId = Long.parseLong(vehicleString.split(" - ")[0]);

            LocalDate selectedDate = datePicker.getValue();
            LocalDate today = LocalDate.now();
            if (selectedDate == null) {
                messages.showError(getString("booking.error.select_date"));
                return;
            }
            if (selectedDate.isBefore(today)) {
                messages.showError(getString("booking.error.before_date"));
                return;
            }
            if(descriptionField.getText().length()> 200){
                messages.showError(getString("booking.error.desc_too_long"));
                return;}
            String description = descriptionField.getText();


            if (mechanicField == null) {
                return;
            }


            String mechanicString = mechanicField.getValue();
            if (mechanicString == null) {
                messages.showError(getString("booking.error.select_mechanic"));
                return;
            }
            Long mechanicId = Long.parseLong(mechanicString.split(" - ")[0]);

            List<ServiceItem> selectedServices = servicesListView.getItems().stream()
                    .filter(item -> serviceSelections.containsKey(item.getId()) && serviceSelections.get(item.getId()).get())
                    .collect(Collectors.toList());

            if (selectedServices.isEmpty()) {
                messages.showError(getString("booking.error.select_services"));
                return;
            }

            bookingService.saveBooking(vehicleId, selectedDate, description, mechanicId, selectedServices);
            navigateToBookingView();

        } catch (Exception e) {
            logger.error("Could not save booking to database. {}", e.getMessage(), e);
            messages.showError(getString("booking.error.unexpected"));
        }
    }

    @FXML
    private void handleCancel() {
        messages.clearMessage();
        navigateToBookingView();
    }

    private void navigateToBookingView() {
        loadCenterView("/com/wac/autocore/gui/view/BookingView.fxml");
    }

    // findMainLayout() och loadCenterView(...) är nu borttagna
    // härifrån eftersom de ärvs direkt från OverController!
}