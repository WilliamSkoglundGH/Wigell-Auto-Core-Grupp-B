package com.wac.autocore.gui.controller;

import com.wac.autocore.dto.booking.BookingCloneDto;

import com.wac.autocore.exception.BookingNotFoundException;

import com.wac.autocore.model.*;
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
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
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
    @FXML private TableColumn<Booking, String> estimateColumn;

    // FORM FIELDS (NewBookingView.fxml)
    @FXML private ComboBox<ServicePackage> packageCombo;
    @FXML private ComboBox<String> vehicleCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionField;
    @FXML private Button saveButton;
    @FXML private ComboBox<String> mechanicField;
    @FXML private ListView<ServiceItem> servicesListView;

    // COPIED NEW BOOKING FORM (NewCopiedBooking.fxml)
    @FXML private Label vehicleCopiedLabel;
    @FXML private DatePicker datePickerCopied;
    @FXML private ListView<ServiceItem> servicesListViewCopied;
    @FXML private TextArea descriptionFieldCopied;
    @FXML private ComboBox<String> mechanicFieldCopied;


    // SERVICES VIEW (BookingServicesView.fxml)
    @FXML private TableView<BookingServiceItem> bookingServicesTable;
    @FXML private TableColumn<BookingServiceItem, String> serviceNameColumn;
    @FXML private TableColumn<BookingServiceItem, BigDecimal> servicePriceColumn;
    @FXML private TableColumn<BookingServiceItem, Integer> serviceDurationColumn;
    @FXML private Button removeServiceButton;
    @FXML private Button addServiceButton;
    @FXML private Label servicesLockedLabel;

    private static Long currentBookingId;


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
                    new SimpleStringProperty(
                            cellData.getValue().getVehicle().getRegistrationNumber()
                    )
            );
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            statusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            getString("status." + cellData.getValue().getStatus())));
            mechanicColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            cellData.getValue().getMechanic().getName()));
            estimateColumn.setCellValueFactory(cellData -> {
                if (cellData.getValue().getServiceItems().isEmpty()) {
                    return new SimpleStringProperty(
                            getString("booking.estimate.missing"));
                }
                int totalMinutes = 0;
                BigDecimal totalPrice = BigDecimal.ZERO;

                for (BookingServiceItem item : cellData.getValue().getServiceItems()) {
                    totalMinutes += item.getDurationAtTime();
                    totalPrice = totalPrice.add(item.getPriceAtTime());
                }

                return new SimpleStringProperty(
                        totalMinutes + " min | " + totalPrice.toPlainString() + " SEK"
                );
            });

            loadBookingData();
            bookingTable.requestFocus();

            bookingTable.setRowFactory(tv -> {
                TableRow<Booking> row = new TableRow<>();
                row.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && !row.isEmpty()) {
                        handleShowBookingServices();
                    }
                });
                return row;
            });
        }

        // BookingServicesView.fxml
        if (bookingServicesTable != null) {
            serviceNameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            cellData.getValue().getServiceItem().getName()
                    )
            );

            servicePriceColumn.setCellValueFactory(
                    new PropertyValueFactory<>("priceAtTime")
            );

            serviceDurationColumn.setCellValueFactory(
                    new PropertyValueFactory<>("durationAtTime")
            );
        }

        // NewBookingView.fxml
        if (vehicleCombo != null) {
            loadVehicleDropdown();
            vehicleCombo.requestFocus();
        }
        if (mechanicField != null) {
            loadMechanicDropdown(mechanicField);
        }
        //NewCopiedBooking.fxml
         if(vehicleCopiedLabel != null && currentBookingId != null) {
             loadCopiedBooking();}
         if(mechanicFieldCopied != null) {
            loadMechanicDropdown(mechanicFieldCopied);
             }


        loadServiceList(servicesListView);
        loadServiceList(servicesListViewCopied);

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
                        bookingService.getAllBookingsWithServiceItems()
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
    //Sätter serviceItem-detailsvyn med selectedbooking från BookingView.

    public void setBooking(Booking booking) {
        if (booking == null) {
            return;
        }
        currentBookingId = booking.getId();
        bookingServicesTable.setItems(
                FXCollections.observableArrayList(booking.getServiceItems())
        );

        BookingState state = booking.getStatus();

        boolean canAdd = state.canAddServiceItem();
        boolean canRemove = state.canRemoveServiceItem();

        addServiceButton.setDisable(!canAdd);
        removeServiceButton.setDisable(!canRemove);

        boolean locked = !canAdd && !canRemove;
        servicesLockedLabel.setVisible(locked);
        servicesLockedLabel.setManaged(locked);

        if (state == BookingState.IN_PROGRESS) {
            servicesLockedLabel.setText(
                    getString("booking.services.locked_in_progress")
            );
        } else if (state == BookingState.COMPLETED) {
            servicesLockedLabel.setText(
                    getString("booking.services.locked_completed")
            );
        }
    }


    private void loadVehicleDropdown() {
        List<String> vehicleList = vehicleService.getAllVehicles().stream()
                .map(v -> v.getId() + " - " +
                        v.getBrand() + " " + v.getModel() +
                        " (" + v.getRegistrationNumber() + ")")
                .collect(Collectors.toList());

        vehicleCombo.setItems(FXCollections.observableArrayList(vehicleList));
    }

    private void loadServiceList(ListView<ServiceItem> listView) {
        if (listView != null) {
            ObservableList<ServiceItem> serviceItems =
                    FXCollections.observableArrayList(serviceItemService.getAllServiceItems());

            for (ServiceItem item : serviceItems) {
                serviceSelections.putIfAbsent(item.getId(),
                        new SimpleBooleanProperty(false));
            }

            listView.setCellFactory(CheckBoxListCell.forListView(
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

            listView.setItems(serviceItems);
        }
    }

    private void loadMechanicDropdown(ComboBox<String> mechanic) {
        List<String> mechanicList = mechanicService.getAllMechanics().stream()
                .map(m -> m.getId() + " - " + m.getName()
                        + " | " + getString("mechanic.specializationColumn")
                        + ": " + m.getSpecialization())
                .collect(Collectors.toList());

        mechanic.setItems(FXCollections.observableArrayList(mechanicList));
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
            String vehicleString = vehicleCombo.getValue();
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
            messages.showSuccess(getString("booking.success.booking_created"));
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

    @FXML
    private void handleBackToBookings() {
        messages.clearMessage();
        navigateToBookingView();
    }

    @FXML
    private void handleRemoveBookingService() {
        BookingServiceItem selectedItem =
                bookingServicesTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            messages.showError(getString("booking.error.select_service_to_remove"));
            return;
        }

        try {
            bookingService.removeServiceItemFromBooking(
                    currentBookingId,
                    selectedItem.getId()
            );

            bookingServicesTable.getItems().remove(selectedItem);
            messages.showSuccess(getString("booking.success.service_removed"));

        } catch (BookingNotFoundException | IllegalArgumentException
                 | IllegalStateException e) {
            messages.showError(getString(e.getMessage()));

        } catch (Exception e) {
            logger.error("Could not remove service from booking.", e);
            messages.showError(getString("booking.error.remove_service"));
        }
    }

    @FXML
    private void handleShowBookingServices() {
        Booking selectedBooking =
                bookingTable.getSelectionModel().getSelectedItem();

        if (selectedBooking == null) {
            messages.showError(getString("booking.error.select_booking"));
            return;
        }

        BookingController controller = loadCenterView(
                "/com/wac/autocore/gui/view/BookingServicesView.fxml"
        );

        if (controller != null) {
            controller.setBooking(selectedBooking);
            messages.clearMessage();
        }
    }

    // ---------------------------------------------------------
    //                BookingService Details View
    // ---------------------------------------------------------

    @FXML
    private void handleAddBookingService() {
        try {
            List<ServiceItem> availableServices =
                    new ArrayList<>(serviceItemService.getAllServiceItems());

            for (BookingServiceItem bookingItem : bookingServicesTable.getItems()) {
                availableServices.removeIf(service ->
                        service.getId().equals(bookingItem.getServiceItem().getId()));
            }

            if (availableServices.isEmpty()) {
                messages.showError(getString("booking.services.none_available"));
                return;
            }

            ChoiceDialog<ServiceItem> dialog =
                    new ChoiceDialog<>(null, availableServices);

            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setHeaderText(getString("booking.services.select"));
            dialog.initOwner(bookingServicesTable.getScene().getWindow());
            dialog.setGraphic(null);
            dialog.getDialogPane().getStyleClass().add("booking-service-dialog");
            dialog.getDialogPane().setPrefHeight(250);

            dialog.getDialogPane().getStylesheets().addAll(
                    bookingServicesTable.getScene().getStylesheets()
            );

            Button confirmButton =
                    (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            confirmButton.setText(getString("booking.services.add"));

            Button cancelButton =
                    (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            cancelButton.setText(getString("btn.cancel"));

            Parent mainView = bookingServicesTable.getScene().getRoot();

            ColorAdjust dimming = new ColorAdjust();
            dimming.setBrightness(-0.35);

            Optional<ServiceItem> selectedService;

            mainView.setEffect(dimming);
            try {
                selectedService = dialog.showAndWait();
            } finally {
                mainView.setEffect(null);
            }

            if (!selectedService.isPresent()) {
                return;
            }

            bookingService.addServiceItemToBooking(
                    currentBookingId,
                    selectedService.get()
            );

            for (Booking booking : bookingService.getAllBookingsWithServiceItems()) {
                if (booking.getId().equals(currentBookingId)) {
                    setBooking(booking);
                    break;
                }
            }

            messages.showSuccess(getString("booking.success.service_added"));

        } catch (BookingNotFoundException | IllegalArgumentException
                 | IllegalStateException e) {
            messages.showError(getString(e.getMessage()));

        } catch (Exception e) {
            logger.error("Could not add service to booking.", e);
            messages.showError(getString("booking.error.add_service"));
        }
    }

    // ---------------------------------------------------------
    //  COPY BOOKING
    // ---------------------------------------------------------

    @FXML
    private void handleCopyBooking(){
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
           // messages.showError(getString("booking.error.select_booking"));
            return; }
        currentBookingId = selected.getId();
        //KOLLA OM BOKNING HAR RÄTT STATUS INNAN GÅR VIDARE
        navigateToNewCopiedBooking();
    }

    @FXML
    private void loadCopiedBooking(){
        if (messages != null) {
            messages.clearMessage();
        }
       if(currentBookingId == null){
            messages.showError(getString("booking.error.select_booking"));
            return;
        }

        BookingCloneDto dto = bookingService.cloneBooking(currentBookingId);
        //För ifyll Vehiclelabel
        System.out.println("efter hämta dto"+dto.toString());
        vehicleCopiedLabel.setText(String.valueOf(dto.getVehicle().toString()));

        applyPreselection(dto);
       }

    private void applyPreselection(BookingCloneDto dto) {
        Set<Long> preSelect = dto.getServiceItems().stream()
                .map(i -> i.getId()).collect(Collectors.toSet());
        serviceSelections.values().forEach(p -> p.set(false));

        //Ändrar utifrån id i HashSet vilka som är true/false så dem blir bockade.
        for (Long id : preSelect) {
            BooleanProperty prop = serviceSelections.get(id);
            if (prop != null) {
                prop.set(true);
            }
        }
    }

    @FXML
    private void handleSaveCopiedBooking() {
        try {
            String vehicleString = vehicleCopiedLabel.getText(); // vad tar jag ut här??
            if (vehicleString == null) {
                messages.showError(getString("booking.error.select_vehicle"));
                return;
            }
            Long vehicleId = Long.parseLong(vehicleString.split(" - ")[0]);

            LocalDate selectedDate = datePickerCopied.getValue();
            LocalDate today = LocalDate.now();
            if (selectedDate == null) {
                messages.showError(getString("booking.error.select_date"));
                return;
            }
            if (selectedDate.isBefore(today)) {
                messages.showError(getString("booking.error.before_date"));
                return;
            }
            if(descriptionFieldCopied.getText().length()> 200){
                messages.showError(getString("booking.error.desc_too_long"));
                return;}
            String description = descriptionFieldCopied.getText();


            if (mechanicFieldCopied == null) {
                return;
            }

            String mechanicString = mechanicFieldCopied.getValue();
            if (mechanicString == null) {
                messages.showError(getString("booking.error.select_mechanic"));
                return;
            }
            Long mechanicId = Long.parseLong(mechanicString.split(" - ")[0]);

            List<ServiceItem> selectedServices = servicesListViewCopied.getItems().stream()
                    .filter(item -> serviceSelections.containsKey(item.getId()) && serviceSelections.get(item.getId()).get())
                    .collect(Collectors.toList());

            if (selectedServices.isEmpty()) {
                messages.showError(getString("booking.error.select_services"));
                return;
            }

            bookingService.saveBooking(vehicleId, selectedDate, description, mechanicId, selectedServices);
            messages.showSuccess(getString("booking.success.booking_created"));
            navigateToBookingView();

        } catch (Exception e) {
            logger.error("Could not save booking to database. {}", e.getMessage(), e);
            messages.showError(getString("booking.error.unexpected"));
        }
    }
    // Skapa en metod för att spara NEw Booking:
    // vehicleCopiedLabel; hämta värde här.
    // DatePicker datePickerCopied;Hämta data här
    // descriptionFieldCopied; hämta data här
    // mechanicFieldCopied; hämta value här

    @FXML
    private void navigateToNewCopiedBooking(){
        loadCenterView("/com/wac/autocore/gui/view/NewCopiedBooking.fxml");
    }

    private void navigateToBookingView() {
        loadCenterView("/com/wac/autocore/gui/view/BookingView.fxml");
    }


}