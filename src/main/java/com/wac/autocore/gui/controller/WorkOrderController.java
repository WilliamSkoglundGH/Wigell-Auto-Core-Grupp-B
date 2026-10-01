package com.wac.autocore.gui.controller;

import com.wac.autocore.dto.workorder.WorkOrderDetailsDto;
import com.wac.autocore.dto.workorder.WorkOrderServiceItemDto;
import com.wac.autocore.dto.workorder.WorkOrderSummaryDto;
import com.wac.autocore.exception.*;
import com.wac.autocore.gui.util.FormatUIUtil;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.BookingState;
import com.wac.autocore.model.WorkOrderState;
import com.wac.autocore.service.BookingService;
import com.wac.autocore.service.ServiceItemService;
import com.wac.autocore.service.WorkOrderService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@Scope("prototype")
public class WorkOrderController extends OverController {

    @FXML
    private TableView<WorkOrderSummaryDto> workOrderTable;
    @FXML
    private TableColumn<WorkOrderSummaryDto, Long> idColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> bookingIdColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> mechanicIdColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> statusColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> startTimeColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> endTimeColumn;
    @FXML
    private Button detailsButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Label mechanicLabel;
    @FXML
    private Label vehicleLabel;
    @FXML
    private Label customerLabel;
    @FXML
    private Label startTimeLabel;
    @FXML
    private Label endTimeLabel;
    @FXML
    private Label estTimeLabel;
    @FXML
    private Label estPriceLabel;
    @FXML
    private Label bookingIdLabel;
    @FXML
    private TableView<WorkOrderServiceItemDto> serviceItemTable;
    @FXML
    private TableColumn<WorkOrderServiceItemDto, String> serviceNameColumn;
    @FXML
    private TableColumn<WorkOrderServiceItemDto, BigDecimal> servicePriceColumn;
    @FXML
    private TableColumn<WorkOrderServiceItemDto, Integer> serviceDurationColumn;
    @FXML
    private Button startButton;
    @FXML
    private Button completeButton;


    @FXML
    private ComboBox<Booking> bookingComboBox;
 //   @FXML
 //   private ListView<ServiceItem> servicesListView;

    private final Map<Long, javafx.beans.property.BooleanProperty> serviceSelections = new HashMap<>();

    private static Long currentWorkOrderId;

    private final WorkOrderService workOrderService;
    private final ServiceItemService serviceItemService;
    private final BookingService bookingService;

    private static final Logger logger = LoggerFactory.getLogger(WorkOrderController.class);

    // Spring injicerar tjänster samt ApplicationContext
    public WorkOrderController(BookingService bookingService, ServiceItemService serviceItemService,
                               WorkOrderService workOrderService, ApplicationContext applicationContext) {
        this.bookingService = bookingService;
        this.serviceItemService = serviceItemService;
        this.workOrderService = workOrderService;
        this.applicationContext = applicationContext;
    }

    // ---------------------------------------------------------
    // INITIALIZE
    // ---------------------------------------------------------
    @FXML
    public void initialize() {
        // WorkOrderView.fxml
        if (workOrderTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            bookingIdColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBookingId().toString()));

            mechanicIdColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMechanicName()));

            statusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            getString("status." + cellData.getValue().getStatus())
                    )
            );

            startTimeColumn.setCellValueFactory(cellData -> {
                LocalDateTime startTime = cellData.getValue().getStartTime();
                String formattedTime = (startTime != null) ? FormatUIUtil.formatTime(startTime) : "";
                return new javafx.beans.property.SimpleStringProperty(formattedTime);
            });

            endTimeColumn.setCellValueFactory(cellData -> {
                LocalDateTime endTime = cellData.getValue().getEndTime();
                String formattedTime = (endTime != null) ? FormatUIUtil.formatTime(endTime) : "";
                return new javafx.beans.property.SimpleStringProperty(formattedTime);
            });
            if (detailsButton != null) {
                detailsButton.disableProperty().bind(
                        workOrderTable.getSelectionModel().selectedItemProperty().isNull()
                );
            }

            loadWorkOrderData();
            workOrderTable.requestFocus();
            workOrderTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    WorkOrderSummaryDto selectedOrder = workOrderTable.getSelectionModel().getSelectedItem();
                    if (selectedOrder != null) {
                        currentWorkOrderId = selectedOrder.getId();
                        navigateToWorkOrderInfoView();
                    }
                }
            });
        }

        loadBookingComboBox();
        if (statusLabel != null && currentWorkOrderId != null) {
            loadWorkOrderDetails();
        }
    }


    public void loadWorkOrderData() {
        if (workOrderTable != null) {
            try {
                ObservableList<WorkOrderSummaryDto> workOrderData = FXCollections.observableArrayList(
                        workOrderService.getAllWorkOrders()
                );
                workOrderTable.setItems(workOrderData);
            } catch (Exception e) {
                logger.error("Could not load work orders from database. {}", e.getMessage(), e);
            }
        }
    }

    @FXML
    private void handleStartWorkOrder() {

        try {
            workOrderService.startWorkOrder(currentWorkOrderId);
            loadWorkOrderDetails();
            messages.showSuccess(getString("workorder.success.workorder_started"));
        } catch (MechanicNotAvailableException e) {
            messages.showError(getString("workorder.error.mechanic_not_available"));

        } catch (BookingWithoutServicesException e) {
            messages.showError(getString("workorder.error.booking_without_service_item"));

        } catch (BookingWrongStatusException e) {
            messages.showError(getString("workorder.error.booking_not_state_created"));

        } catch (Exception e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    @FXML
    private void handleCompleteWorkOrder() {


        try {
            workOrderService.completeWorkOrder(currentWorkOrderId);
            loadWorkOrderDetails();
            messages.showSuccess(getString("workorder.success.workorder_completed"));
            loadWorkOrderData();

        }catch (WorkOrderWrongStatusException e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.work_order_wrong_status"));
        } catch (WorkOrderNotFoundException e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.work_order_not_found"));
        }catch (Exception e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    @FXML
    private void handleNewWorkOrder() {
        if (messages != null) {
            messages.clearMessage();
        }
        loadCenterView("/com/wac/autocore/gui/view/NewWorkOrderView.fxml");
    }

    @FXML
    private void handleDetails() {
        Long id  = workOrderTable.getSelectionModel().getSelectedItem().getId();
        if (id == null) {
            return;
        }
        currentWorkOrderId = id;
        navigateToWorkOrderInfoView();
    }

    private void loadBookingComboBox() {
        if (bookingComboBox != null) {
            try {
                List<Booking> bookedList = bookingService.getAllBookings().stream()
                        //.filter(b -> "BOOKED".equalsIgnoreCase(b.getStatus()))
                        .filter(b -> BookingState.BOOKED.equals(b.getStatus()))
                        .collect(Collectors.toList());
//
                bookingComboBox.setItems(FXCollections.observableArrayList(bookedList));

                bookingComboBox.setConverter(new StringConverter<Booking>() {
                    @Override
                    public String toString(Booking booking) {
                        String description = booking.getDescription();

                        return getString("booking.option.id") + ": " + booking.getId()
                                + " | " + getString("booking.option.vehicle") + ": "
                                + booking.getVehicle().getRegistrationNumber()
                                + " | " + getString("booking.option.date") + ": "
                                + booking.getDate()
                                + " | " + getString("booking.option.mechanic") + ": "
                                + booking.getMechanic().getName()
                                + (description == null || description.trim().isEmpty()
                                ? ""
                                : " | " + getString("booking.option.description")
                                  + ": " + description);
                    }

                    @Override
                    public Booking fromString(String s) {
                        return null;
                    }
                });
            } catch (NullPointerException e) {
                messages.showError(getString("workorder.error.no_bookings"));
            }
            bookingComboBox.requestFocus();
        }
    }

/*    private void loadServiceList() {
        if (servicesListView != null) {
            ObservableList<ServiceItem> serviceItems = FXCollections.observableArrayList(serviceItemService.getAllServiceItems());
            servicesListView.setItems(serviceItems);

            for (ServiceItem item : serviceItems) {
                serviceSelections.putIfAbsent(item.getId(), new SimpleBooleanProperty(false));
            }
            servicesListView.setCellFactory(CheckBoxListCell.forListView(
                    item -> serviceSelections.get(item.getId()),
                    new StringConverter<ServiceItem>() {
                        @Override
                        public String toString(ServiceItem item) {
                            return item.getId() + ": " + item.getName();
                        }

                        @Override
                        public ServiceItem fromString(String string) {
                            return null;
                        }
                    }
            ));
        }
    }
*/
    @FXML
    private void handleSaveWorkOrder() {
        try {
            if (bookingComboBox != null) {
                Booking selectedBooking = bookingComboBox.getValue();
                if (selectedBooking == null) {
                    messages.showError(getString("workorder.error.select_booking"));
                    return;
                }
                      selectedBooking.setStatus(selectedBooking.getStatus().getNext());

                    workOrderService.saveWorkOrder(selectedBooking.getId());
                    messages.showSuccess(getString("workorder.success.workorder_created"));

            }
        } catch (Exception e) {
            logger.error("Something Unexpected. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
            return;
        }

        serviceSelections.clear();
        navigateToWorkOrderView();
    }

    @FXML
    private void handleCancel() {
        if (messages != null) {
            messages.clearMessage();
        }
        navigateToWorkOrderView();
    }

    private void navigateToWorkOrderView() {
        loadCenterView("/com/wac/autocore/gui/view/WorkOrderView.fxml");
    }

    private void navigateToWorkOrderInfoView() {
        loadCenterView("/com/wac/autocore/gui/view/WorkOrderInfoView.fxml");

    }

    private void loadWorkOrderDetails(){
        if (messages != null) {
            messages.clearMessage();
        }
        try {
            //hämta workorders och ladda labels
            WorkOrderDetailsDto dto = workOrderService.getWorkOrderInfoById(currentWorkOrderId);
            statusLabel.setText(getString(dto.getStatus().name()));
            mechanicLabel.setText(dto.getMechanicName());
            vehicleLabel.setText(dto.getVehicleRegistrationNumber());
            customerLabel.setText(dto.getCustomerName());
            startTimeLabel.setText(dto.getStartTime() != null ? FormatUIUtil.formatTime(dto.getStartTime()) : "-");
            endTimeLabel.setText(dto.getEndTime() != null ? FormatUIUtil.formatTime(dto.getEndTime()) : "-");

            estTimeLabel.setText(dto.getEstimatedDuration() != null ? dto.getEstimatedDuration() + " min" : "-");
            estPriceLabel.setText(dto.getEstimatedPrice() != null ? dto.getEstimatedPrice() + " SEK" : "-");
            bookingIdLabel.setText(String.valueOf(dto.getBookingId()));

            updateButtonStates(dto.getStatus());

            serviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
            servicePriceColumn.setCellValueFactory(new PropertyValueFactory<>("priceAtTime"));
            serviceDurationColumn.setCellValueFactory(new PropertyValueFactory<>("durationAtTime"));
        populateServiceItemsTable(dto);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void populateServiceItemsTable(WorkOrderDetailsDto dto) {
        if (dto != null && dto.getServiceItems() != null) {

            List<WorkOrderServiceItemDto> dtoList = dto.getServiceItems();

            ObservableList<WorkOrderServiceItemDto> observableList = FXCollections.observableArrayList(dtoList);

            serviceItemTable.getItems().setAll(observableList);
        }
    }
    private void updateButtonStates(WorkOrderState status) {
        if (startButton == null || completeButton == null) {
            return;
        }

        if (status == null) {
            startButton.setDisable(true);
            completeButton.setDisable(true);
            return;
        }

        startButton.setDisable(!status.canStart());
        completeButton.setDisable(!status.canComplete());
    }
}