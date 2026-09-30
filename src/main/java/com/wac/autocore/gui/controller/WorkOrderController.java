package com.wac.autocore.gui.controller;

import com.wac.autocore.dto.WorkOrderSummaryDto;
import com.wac.autocore.exception.BookingWithoutServicesException;
import com.wac.autocore.exception.MechanicNotAvailableException;
import com.wac.autocore.gui.util.FormatUIUtil;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.BookingServiceItem;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.service.BookingService;
import com.wac.autocore.service.ServiceItemService;
import com.wac.autocore.service.WorkOrderService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private TableColumn<WorkOrderSummaryDto, Integer> estimatedTimeColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> startTimeColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> endTimeColumn;

    @FXML
    private ComboBox<Booking> bookingComboBox;
 //   @FXML
 //   private ListView<ServiceItem> servicesListView;

    private final Map<Long, javafx.beans.property.BooleanProperty> serviceSelections = new HashMap<>();

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

            estimatedTimeColumn.setCellValueFactory(cellData ->
                    // Om ni vill ha beräknad tid i DTO:n lägger ni till fältet där.
                    // Annars kan ni ha ett fast värde så länge:
                    new javafx.beans.property.SimpleObjectProperty<>(120));

            loadWorkOrderData();
            workOrderTable.requestFocus();
        }

        // NewWorkOrderView.fxml
        loadBookingComboBox();
        //loadServiceList();
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
        WorkOrderSummaryDto selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            messages.showError(getString("workorder.error.choose_workorder"));
            return;
        }
        try {
            workOrderService.startWorkOrder(selected.getId());
            loadWorkOrderData();
            messages.showSuccess(getString("workorder.success.workorder_started"));
        } catch (MechanicNotAvailableException e) {
            messages.showError(getString("workorder.error.mechanic_not_available"));

        } catch (BookingWithoutServicesException e) {
            messages.showError(getString("workorder.error.booking_without_service_item"));

        } catch (Exception e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    @FXML
    private void handleCompleteWorkOrder() {
        WorkOrderSummaryDto selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            messages.showError(getString("workorder.error.choose_workorder"));
            return;
        }
        if ("COMPLETED".equals(selected.getStatus())) {
            messages.showError(getString("workorder.error.workorder_already_completed"));
            return;
        }
        if ("CREATED".equals(selected.getStatus())) {
            messages.showError(getString("workorder.error.workorder_not_started"));
            return;
        }
        try {
            workOrderService.completeWorkOrder(selected.getId());
            workOrderTable.refresh();
            messages.showSuccess(getString("workorder.success.workorder_completed"));
            loadWorkOrderData();
        } catch (Exception e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    @FXML
    private void handleNewWorkOrder() {
        if (messages != null) {
            messages.clearMessage();
        }
        // Använder OverControllers gemensamma metod
        loadCenterView("/com/wac/autocore/gui/view/NewWorkOrderView.fxml");
    }

    private void loadBookingComboBox() {
        if (bookingComboBox != null) {
            try {
                List<Booking> bookedList = bookingService.getAllBookings().stream()
                        .filter(b -> "BOOKED".equalsIgnoreCase(b.getStatus()))
                        .collect(Collectors.toList());

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
                      selectedBooking.setStatus("WORK_ORDER_CREATED");

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
}