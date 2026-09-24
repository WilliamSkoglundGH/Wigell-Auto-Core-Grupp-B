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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class WorkOrderController extends OverController {

    @FXML private TableView<WorkOrder> workOrderTable;
    @FXML private TableColumn<WorkOrder, Integer> idColumn;
    @FXML private TableColumn<WorkOrder, Integer> bookingIdColumn;
    @FXML private TableColumn<WorkOrder, Integer> mechanicIdColumn;
    @FXML private TableColumn<WorkOrder, String> statusColumn;
    @FXML private TableColumn<WorkOrder, String> servicesColumn;
    @FXML private TableColumn<WorkOrder,Integer> estimatedTimeColumn;
    @FXML private TableColumn<WorkOrder, LocalDateTime> startTimeColumn;

    @FXML private ComboBox<Booking> bookingComboBox;
    @FXML private ComboBox<Mechanic> mechanicComboBox;
    @FXML private ListView<ServiceItem> servicesListView;

    private final java.util.Set<Integer> selectedServiceIds = new java.util.HashSet<>();
    private final java.util.Map<Long, javafx.beans.property.BooleanProperty> serviceSelections = new java.util.HashMap<>();

        private WorkOrderService workOrderService;
        private ServiceItemService serviceItemService;
        private BookingService bookingService;
        private static final Logger logger = LoggerFactory.getLogger(com.wac.autocore.gui.controller.WorkOrderController.class);

        public WorkOrderController(BookingService bookingService,ServiceItemService serviceItemService, WorkOrderService workOrderService) {
            this.bookingService = bookingService;
            this.serviceItemService = serviceItemService;
            this.workOrderService = workOrderService;
        }

        // ---------------------------------------------------------
        // INITIALIZE
        // ---------------------------------------------------------
        @FXML
        public void initialize() {
            if (workOrderTable != null) {
                idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
                bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
                mechanicIdColumn.setCellValueFactory(new PropertyValueFactory<>("mechanicId"));
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
                estimatedTimeColumn.setCellValueFactory(cellData ->
                        new SimpleObjectProperty<>(countTotalMin(cellData.getValue().getServiceItems())));

                startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("start_time"));
                if(startTimeColumn!=null){
                    startTimeColumn.setCellFactory(column -> new TableCell<WorkOrder, LocalDateTime>() {
                        @Override
                        protected void updateItem(LocalDateTime item, boolean empty) {
                            super.updateItem(item, empty);
                            setText((empty || item == null) ? "" : item.toString()); // Formatera för snyggare datetime??
                        }
                    });
                }
                if (servicesColumn != null) {
                    servicesColumn.setCellValueFactory(cellData -> {
                        WorkOrder wo = cellData.getValue();
                        if (wo.getServiceItems() == null || wo.getServiceItems().isEmpty()) {
                            return new SimpleStringProperty("No services");
                        }
                        // Skapa en sträng med "ID: Namn" för varje tjänst
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
            }
        }

        public int countTotalMin(List<ServiceItem> serviceItems) {
            int totalMin = 0;
            for (ServiceItem s : serviceItems) {
                totalMin += s.getEstimatedMinutes();}
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
            try{ workOrderService.startWorkOrder(selected);
                   workOrderTable.refresh();
                    messages.showSuccess("Work order " + selected.getId() + " has been started.");
                }
                catch (Exception e){
                    logger.error("Could not save to database. {}", e.getMessage(), e);
                    messages.showError("An unexpected error occurred. Please check the list of work orders before trying again.");
                    // stapla dem andra exceptionsen?
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
            try{ workOrderService.completeWorkOrder(selected);
            workOrderTable.refresh();
            messages.showSuccess("Work order " + selected.getId() + " has been completed.");
            }catch (Exception e){
                logger.error("Could not save to database. {}", e.getMessage(), e);
                messages.showError("An unexpected error occurred. Please check the list of work orders before trying again.");
            }
        }

        @FXML
        private void handleNewWorkOrder() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/NewWorkOrderView.fxml"));
                loader.setController(this);
                Parent newWorkOrderView = loader.load();

                populateComboBoxes();

                BorderPane mainLayout = findMainLayout();
                if (mainLayout != null) {
                    mainLayout.setCenter(newWorkOrderView);
                    bookingComboBox.requestFocus();
                } else {
                    messages.showError("Could not find BorderPane!");
                }
            } catch (IOException e) {
                e.printStackTrace();
                messages.showError("Could not load NewWorkOrderView.fxml: " + e.getMessage());
            }
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

                // Skapa upp en boolean-egenskap för varje tjänst om den inte finns
                for (ServiceItem item : serviceItems) {
                    serviceSelections.putIfAbsent(item.getId(), new SimpleBooleanProperty(false));
                }

                // Använd JavaFXs inbyggda CheckBoxListCell som hanterar cell-återanvändning galant
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
            try{
                if (bookingComboBox != null ) {
                    Booking selectedBooking = bookingComboBox.getValue();
                    if (selectedBooking != null) {
                        messages.showError("Please select a booking");
                        return;
                    }
                    // Hämta alla tjänster som markerats i ListView

                    List<ServiceItem> serviceItems = servicesListView.getSelectionModel().getSelectedItems();

                    // Skapa och spara arbetsordern.
                    workOrderService.saveWorkOrder(selectedBooking.getId(), serviceItems);
                    selectedBooking.setStatus("WORK_ORDER_CREATED");
                }
                } catch (Exception e) {
                    logger.error("Could not save to database. {}", e.getMessage(), e);
                    messages.showError("An unexpected error occurred. Please check the list of work orders before trying again.");
                }
                    // Rensa valen och navigera
                    serviceSelections.clear();
                    messages.showSuccess("Work order created.");
                    navigateToWorkOrderView();

        }

        @FXML
        private void handleCancel() {
            navigateToWorkOrderView();
        }

        private void navigateToWorkOrderView() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/WorkOrderView.fxml"));
                Parent workOrderView = loader.load();

                // Hämta den nya kontrollern och skicka med messages!
                com.wac.autocore.gui.controller.WorkOrderController controller = loader.getController();
                if (controller != null) {
                    controller.setMessages(this.messages);
                }

                BorderPane mainLayout = findMainLayout();
                if (mainLayout != null) {
                    mainLayout.setCenter(workOrderView);
                    controller.workOrderTable.requestFocus();
                } else {
                    if (messages != null) {
                        messages.showError("Could not find BorderPane!");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (messages != null) {
                    messages.showError("Could not load WorkOrderView.fxml: " + e.getMessage());
                }
            }
        }

        private BorderPane findMainLayout() {
            Scene scene = null;

            if (bookingComboBox != null && bookingComboBox.getScene() != null) {
                scene = bookingComboBox.getScene();
            } else if (workOrderTable != null && workOrderTable.getScene() != null) {
                scene = workOrderTable.getScene();
            }

            if (scene != null && scene.getRoot() != null) {
                Parent root = scene.getRoot();
                if (root instanceof BorderPane) {
                    return (BorderPane) root;
                }
                return searchBorderPaneRecursive(root);
            }
            return null;
        }
    }
