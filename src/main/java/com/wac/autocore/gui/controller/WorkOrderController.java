package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.service.BookingService;
import com.wac.autocore.service.MechanicService;
import com.wac.autocore.service.ServiceItemService;
import com.wac.autocore.service.WorkOrderService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
public class WorkOrderController extends OverController {

    @FXML private TableView<WorkOrder> workOrderTable;
    @FXML private TableColumn<WorkOrder, Integer> idColumn;
    @FXML private TableColumn<WorkOrder, Integer> bookingIdColumn;
    @FXML private TableColumn<WorkOrder, Integer> mechanicIdColumn;
    @FXML private TableColumn<WorkOrder, String> statusColumn;
    @FXML private TableColumn<WorkOrder, String> servicesColumn;
    @FXML private TableColumn<WorkOrder, Integer> estimatedTimeColumn;
    @FXML private TableColumn<WorkOrder, LocalDateTime> startTimeColumn;

    @FXML private ComboBox<Booking> bookingComboBox;
    @FXML private ComboBox<Mechanic> mechanicComboBox;
    @FXML private ListView<ServiceItem> servicesListView;

    private final Set<Integer> selectedServiceIds = new HashSet<>();
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
            bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
            mechanicIdColumn.setCellValueFactory(new PropertyValueFactory<>("mechanicId"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            estimatedTimeColumn.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(countTotalMin(cellData.getValue().getServiceItems())));

            startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("start_time"));
            if (startTimeColumn != null) {
                startTimeColumn.setCellFactory(column -> new TableCell<WorkOrder, LocalDateTime>() {
                    @Override
                    protected void updateItem(LocalDateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        setText((empty || item == null) ? "" : item.toString());
                    }
                });
            }
            if (servicesColumn != null) {
                servicesColumn.setCellValueFactory(cellData -> {
                    WorkOrder wo = cellData.getValue();
                    if (wo.getServiceItems() == null || wo.getServiceItems().isEmpty()) {
                        return new SimpleStringProperty("No services");
                    }
                    String serviceInfo = wo.getServiceItems().stream()
                            .map(id -> serviceItemService.getAllServiceItems()
                                    .stream()
                                    .filter(s -> s.getId() == id.getId())
                                    .map(s -> s.getId() + ": " + s.getName())
                                    .findFirst()
                                    .orElse("ID " + id + ": Unknown"))
                            .collect(Collectors.joining(", "));
                    return new SimpleStringProperty(serviceInfo);
                });
            }
            loadWorkOrderData();
            workOrderTable.requestFocus();
        }

        // NewWorkOrderView.fxml
        if (bookingComboBox != null) {
            populateComboBoxes();
            bookingComboBox.requestFocus();
        }
    }

    public int countTotalMin(List<ServiceItem> serviceItems) {
        int totalMin = 0;
        if (serviceItems != null) {
            for (ServiceItem s : serviceItems) {
                totalMin += s.getEstimatedMinutes();
            }
        }
        return totalMin;
    }

    public void loadWorkOrderData() {
        if (workOrderTable != null) {
            ObservableList<WorkOrder> workOrderData = FXCollections.observableArrayList(
                    workOrderService.getAllWorkOrders()
            );
            workOrderTable.setItems(workOrderData);
        }
    }

    @FXML
    private void handleStartWorkOrder() {
        WorkOrder selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            messages.showError("Please choose a work order to start.");
            return;
        }
        try {
            workOrderService.startWorkOrder(selected);
            workOrderTable.refresh();
            messages.showSuccess("Work order " + selected.getId() + " has been started.");
        } catch (Exception e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError("An unexpected error occurred. Please check the list of work orders before trying again.");
        }
    }

    @FXML
    private void handleCompleteWorkOrder() {
        WorkOrder selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            messages.showError("Please choose a work order to complete.");
            return;
        }
        if ("COMPLETED".equals(selected.getStatus())) {
            messages.showError("Work order already COMPLETED");
            return;
        }
        try {
            workOrderService.completeWorkOrder(selected);
            workOrderTable.refresh();
            messages.showSuccess("Work order " + selected.getId() + " has been completed.");
        } catch (Exception e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError("An unexpected error occurred. Please check the list of work orders before trying again.");
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

    private void populateComboBoxes() {
        if (bookingComboBox != null) {
            List<Booking> bookedList = bookingService.getAllBookings().stream()
                    .filter(b -> "BOOKED".equalsIgnoreCase(b.getStatus()))
                    .collect(Collectors.toList());

            bookingComboBox.setItems(FXCollections.observableArrayList(bookedList));
        }

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
                            return item != null ? item.getId() + ": " + item.getName() : "";
                        }
                        @Override
                        public ServiceItem fromString(String string) {
                            return null;
                        }
                    }
            ));
        }
    }

    @FXML
    private void handleSaveWorkOrder() {
        try {
            if (bookingComboBox != null) {
                Booking selectedBooking = bookingComboBox.getValue();
                if (selectedBooking == null) {
                    messages.showError("Please select a booking");
                    return;
                }

                // Samla ihop valda tjänster baserat på vilka checkboxes som är markerade i mappen
                List<ServiceItem> serviceItems = serviceItemService.getAllServiceItems().stream()
                        .filter(item -> serviceSelections.containsKey(item.getId()) && serviceSelections.get(item.getId()).get())
                        .collect(Collectors.toList());

                workOrderService.saveWorkOrder(selectedBooking.getId(), serviceItems);
                selectedBooking.setStatus("WORK_ORDER_CREATED");
            }
        } catch (Exception e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError("An unexpected error occurred. Please check the list of work orders before trying again.");
            return;
        }

        serviceSelections.clear();
        messages.showSuccess("Work order created.");
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

    // findMainLayout() är helt borttagen eftersom OverController sköter det centralt!
}