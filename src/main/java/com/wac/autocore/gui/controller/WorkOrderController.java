package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.service.BookingService;
import com.wac.autocore.service.MechanicService;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@Scope("prototype")
public class WorkOrderController extends OverController {

    @FXML private TableView<WorkOrder> workOrderTable;
    @FXML private TableColumn<WorkOrder, Long> idColumn;
    @FXML private TableColumn<WorkOrder, String> bookingIdColumn;
    @FXML private TableColumn<WorkOrder, String> mechanicIdColumn;
    @FXML private TableColumn<WorkOrder, String> statusColumn;
    @FXML private TableColumn<WorkOrder, String> servicesColumn;
    @FXML private TableColumn<WorkOrder, Integer> estimatedTimeColumn;
    @FXML private TableColumn<WorkOrder, LocalDateTime> startTimeColumn;

    @FXML private ComboBox<Booking> bookingComboBox;
    @FXML private ListView<ServiceItem> servicesListView;

    private final Set<Integer> selectedServiceIds = new HashSet<>(); // Används ej.
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
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBooking().getId().toString()));
            mechanicIdColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBooking().getMechanic().getId() + " - " + cellData.getValue().getBooking().getMechanic().getName()));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            estimatedTimeColumn.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(countTotalMin(cellData.getValue().getServiceItems())));

            startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
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
        loadBookingComboBox();
        loadServiceList();
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
//TODO Fixa Null pointer problematiken
    public void loadWorkOrderData() {
        if (workOrderTable != null) {
            try {
                ObservableList<WorkOrder> workOrderData = FXCollections.observableArrayList(
                        workOrderService.getAllWorkOrders()
                );
                workOrderTable.setItems(workOrderData);
            }
            catch (NullPointerException e){
                logger.error("Nullpointer i workorder-databasen. {}", e.getMessage(), e);
            }
            catch (Exception e){
                logger.error("Could not load work orders from database. {}", e.getMessage(), e);
                } }}

    @FXML
    private void handleStartWorkOrder() {
        WorkOrder selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            messages.showError(getString("workorder.error.choose_workorder"));
            return;
        }
        try {
            workOrderService.startWorkOrder(selected);
            workOrderTable.refresh();
            messages.showSuccess(getString("workorder.success.workorder_started"));
        } catch (Exception e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    @FXML
    private void handleCompleteWorkOrder() {
        WorkOrder selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            messages.showError(getString("workorder.error.choose_workorder"));
            return;
        }
        if ("COMPLETED".equals(selected.getStatus())) {
            messages.showError(getString("workorder.error.workorder_already_completed"));
            return;
        }
        try {
            workOrderService.completeWorkOrder(selected);
            workOrderTable.refresh();
            messages.showSuccess(getString("workorder.success.workorder_completed"));
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
            }catch (NullPointerException e){
                 messages.showError(getString("workorder.error.no_bookings"));
            }
            bookingComboBox.requestFocus();
        }
    }

    private void loadServiceList() {
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
                    messages.showError(getString("workorder.error.select_booking"));
                    return;
                }

                // Samla ihop valda tjänster baserat på vilka checkboxes som är markerade i mappen
                List<ServiceItem> serviceItems = serviceItemService.getAllServiceItems().stream()
                        .filter(item -> serviceSelections.containsKey(item.getId()) && serviceSelections.get(item.getId()).get())
                        .collect(Collectors.toList());

                selectedBooking.setStatus("WORK_ORDER_CREATED");
                workOrderService.saveWorkOrder(selectedBooking.getId(),serviceItems);

            }
        } catch (Exception e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
            return;
        }

        serviceSelections.clear();
        messages.showSuccess(getString("workorder.success.workorder_created"));
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