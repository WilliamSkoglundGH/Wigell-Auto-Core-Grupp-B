package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.service.BookingService;
import com.wac.autocore.service.MechanicService;
import com.wac.autocore.service.ServiceItemService;
import com.wac.autocore.service.WorkOrderService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    @FXML private ComboBox<Booking> bookingComboBox;
    @FXML private ComboBox<Mechanic> mechanicComboBox;
    @FXML private ListView<ServiceItem> servicesListView;

    private UserMessages messages;

    private final java.util.Set<Integer> selectedServiceIds = new java.util.HashSet<>();
    private final java.util.Map<Long, javafx.beans.property.BooleanProperty> serviceSelections = new java.util.HashMap<>();

        private WorkOrderService workOrderService;
        private ServiceItemService serviceItemService;
        private MechanicService mechanicService;
        private BookingService bookingService;
        private static final Logger logger = LoggerFactory.getLogger(com.wac.autocore.gui.controller.WorkOrderController.class);

        public WorkOrderController(BookingService bookingService, MechanicService mechanicService, ServiceItemService serviceItemService, WorkOrderService workOrderService) {
            this.bookingService = bookingService;
            this.mechanicService = mechanicService;
            this.serviceItemService = serviceItemService;
            this.workOrderService = workOrderService;
        }
        //TODO Loggning och exceptions. Lösa ServiceItem-listan.
        // ---------------------------------------------------------
        // INITIALIZE
        // ---------------------------------------------------------
        @FXML
        public void initialize() {
            //Lägg till estamated time- column
            if (workOrderTable != null) {
                idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
                bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
                mechanicIdColumn.setCellValueFactory(new PropertyValueFactory<>("mechanicId"));
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
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

        //Läggas som hjälpmetod i workorder-enity?
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
        // STARTA WORKORDER-
        // Hämta specifik workorder
        // Ädra workorder till IN_PROGRESS.
        // Hämta specifik mekaniker och sätt som UNAVALEBLE

        @FXML
        private void handleStartWorkOrder() {
            WorkOrder selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
            if (selected != null) {
                if ("CREATED".equals(selected.getStatus())) {
                    selected.setStatus("IN_PROGRESS");

                    Mechanic mechanic = mechanicService.getAllMechanics().stream()
                            .filter(m -> m.getId() == selected.getBooking().getMechanic().getId())
                            .findFirst()
                            .orElse(null);
                    if (mechanic != null) {
                        mechanic.setAvailable(false);
                    }
                    Booking booking = bookingService.getAllBookings().stream()
                            .filter(b -> b.getId() == selected.getBooking().getId())
                            .findFirst()
                            .orElse(null);
                    if (booking != null) {
                        booking.setStatus("IN_PROGRESS");
                    }
                    workOrderService.updateWorkOrder(selected);
                    workOrderTable.refresh();
                    messages.showSuccess("Work order " + selected.getId() + " has been started.");

                } else {
                    messages.showError("A work order must have status CREATED to be started.");
                }
            } else {
                messages.showError("Please choose a work order to start.");
            }
        }
        //COMPLETE WORKORDER
        // Hämta specifik workorder
        // Ändra workorder till COMPLETED.
        // Hämta specifik mekaniker och sätt som AVALABLE

        @FXML
        private void handleCompleteWorkOrder() {
            WorkOrder selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
            if (selected != null) {
                if ("IN_PROGRESS".equals(selected.getStatus())) {
                    selected.setStatus("COMPLETED");

                    // 1. Sätt mekanikern som tillgänglig igen
                    Mechanic mechanic = mechanicService.getAllMechanics().stream()
                            .filter(m -> m.getId() == selected.getId())
                            .findFirst()
                            .orElse(null);
                    if (mechanic != null) {
                        mechanic.setAvailable(true); // Sätt mekanikern som otillgänglig mot databasen också !!!
                    }

                    // 2. Uppdatera bokningens status till COMPLETED.
                    Booking booking = bookingService.getAllBookings().stream()
                            .filter(b -> b.getId() == selected.getId())
                            .findFirst()
                            .orElse(null);
                    if (booking != null) {
                        booking.setStatus("COMPLETED");
                        booking.setMechanic(mechanic);// Lägger uppdaterad mecanic i booking.
                    }

                    workOrderTable.refresh();
                    messages.showSuccess("Work order " + selected.getId() + " has been completed.");
                } else if ("COMPLETED".equals(selected.getStatus())) {
                    messages.showError("Work order already COMPLETED");
                } else {
                    messages.showError("A work order must be started before it can be completed.");
                }
            } else {
                messages.showError("Please choose a work order to complete.");
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
                    if (selectedBooking != null){
                        messages.showError("Please select a booking");
                        return;}

                    // Hämta alla tjänster som markerats i mapen

                    List<ServiceItem> serviceItems = servicesListView.getSelectionModel().getSelectedItems();

                    for (Map.Entry<Long, BooleanProperty> entry : serviceSelections.entrySet()) {
                        if (entry.getValue().get()) {}

                        newWorkOrder.addServiceItem(entry.getKey());}

                    // Skapa och spara arbetsordern.
                    workOrderService.saveWorkOrder(selectedBooking.getId(),serviceItems,"CREATED");

                    selectedBooking.setStatus("WORK_ORDER_CREATED");


                    // Rensa valen och navigera
                    serviceSelections.clear();
                    messages.showSuccess("Work order created.");
                    navigateToWorkOrderView();
                }
            } catch (Exception e) {
                logger.error("Could not save to database. {}", e.getMessage(), e);
                messages.showError("An unexpected error occurred. Please check the list of work orders before trying again.");
            }
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


        public void setMessages(UserMessages messages) {
            this.messages = messages;
        }
    }
    /*
    @FXML
    public void initialize() {
        if (workOrderTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
            mechanicIdColumn.setCellValueFactory(new PropertyValueFactory<>("mechanicId"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            if (servicesColumn != null) {
                servicesColumn.setCellValueFactory(cellData -> {
                    WorkOrder wo = cellData.getValue();
                    if (wo.getServiceItemIds() == null || wo.getServiceItemIds().isEmpty()) {
                        return new javafx.beans.property.SimpleStringProperty("No services");
                    }

                    // Skapa en sträng med "ID: Namn" för varje tjänst
                    String serviceInfo = wo.getServiceItemIds().stream()
                            .map(id -> Database.getServiceItems().stream()
                                    .filter(s -> s.getId() == id)
                                    .map(s -> s.getId() + ": " + s.getName())
                                    .findFirst()
                                    .orElse("ID " + id + ": Unknown"))
                            .collect(Collectors.joining(", "));

                    return new javafx.beans.property.SimpleStringProperty(serviceInfo);
                });
            }
            loadWorkOrderData();
        }
    }

    public void loadWorkOrderData() {
        if (workOrderTable != null) {
            ObservableList<WorkOrder> workOrderData = FXCollections.observableArrayList(
                    Database.getWorkOrders()
            );
            workOrderTable.setItems(workOrderData);
        }
    }

    @FXML
    private void handleStartWorkOrder() {
        WorkOrder selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
        if (selected != null) {
            //sätt booking till IN_PROGRESS
            if ("CREATED".equals(selected.getStatus())) {
                selected.setStatus("IN_PROGRESS");


                Mechanic mechanic = Database.getMechanics().stream()
                        .filter(m -> m.getId() == selected.getMechanicId())
                        .findFirst()
                        .orElse(null);

                if (mechanic != null) {
                    mechanic.setAvailable(false); // Sätt mekanikern som otillgänglig!
                }
                Booking booking = Database.getBookings().stream()
                        .filter(b -> b.getId() == selected.getBookingId())
                        .findFirst()
                        .orElse(null);
                if (booking != null) {
                    booking.setStatus("IN_PROGRESS");
                }
                workOrderTable.refresh();
                messages.showSuccess("Work order " + selected.getId() + " has been started.");
            } else {
                messages.showError("A work order must have status CREATED to be started.");
            }
        } else {
            messages.showError("Please choose a work order to start.");
        }
    }

    @FXML
    private void handleCompleteWorkOrder() {
        WorkOrder selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
        if (selected != null) {
            if ("IN_PROGRESS".equals(selected.getStatus())) {
                selected.setStatus("COMPLETED");
                //sätt booking till COMPLETED

                // 1. Sätt mekanikern som tillgänglig igen
                Mechanic mechanic = Database.getMechanics().stream()
                        .filter(m -> m.getId() == selected.getMechanicId())
                        .findFirst()
                        .orElse(null);
                if (mechanic != null) {
                    mechanic.setAvailable(true);
                }

                // 2. Uppdatera bokningens status till COMPLETED
                Booking booking = Database.getBookings().stream()
                        .filter(b -> b.getId() == selected.getBookingId())
                        .findFirst()
                        .orElse(null);
                if (booking != null) {
                    booking.setStatus("COMPLETED");
                }

                workOrderTable.refresh();
                messages.showSuccess("Work order " + selected.getId() + " has been completed.");
            } else if ("COMPLETED".equals(selected.getStatus())) {
                messages.showError("Work order already COMPLETED");
            } else {
                messages.showError("A work order must be started before it can be completed.");
            }
        } else {
            messages.showError("Please choose a work order to complete.");
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
            List<Booking> bookedList = Database.getBookings().stream()
                    .filter(b -> "BOOKED".equalsIgnoreCase(b.getStatus()))
                    .collect(Collectors.toList());

            bookingComboBox.setItems(FXCollections.observableArrayList(bookedList));
        }

        if (mechanicComboBox != null) {
            List<Mechanic> availableMechanics = Database.getMechanics().stream()
                    .filter(Mechanic::isAvailable)
                    .collect(Collectors.toList());

            mechanicComboBox.setItems(FXCollections.observableArrayList(availableMechanics));
        }
        if (servicesListView != null) {
            ObservableList<ServiceItem> serviceItems = FXCollections.observableArrayList(Database.getServiceItems());
            servicesListView.setItems(serviceItems);

            // Skapa upp en boolean-egenskap för varje tjänst om den inte finns
            for (ServiceItem item : serviceItems) {
                serviceSelections.putIfAbsent(item.getId(), new javafx.beans.property.SimpleBooleanProperty(false));
            }

            // Använd Javes inbyggda CheckBoxListCell som hanterar cell-återanvändning galant
            servicesListView.setCellFactory(javafx.scene.control.cell.CheckBoxListCell.forListView(
                    item -> serviceSelections.get(item.getId()),
                    new javafx.util.StringConverter<ServiceItem>() {
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
        if (bookingComboBox != null && mechanicComboBox != null) {
            Booking selectedBooking = bookingComboBox.getValue();
            Mechanic selectedMechanic = mechanicComboBox.getValue();

            if (selectedBooking != null && selectedMechanic != null) {
                int newId = Database.getWorkOrders().size() + 1;

                // Skapa arbetsordern
                WorkOrder newWorkOrder = new WorkOrder(newId, selectedBooking.getId(), selectedMechanic.getId());

                // Hämta alla tjänster som markerats i mapen
                for (java.util.Map.Entry<Integer, javafx.beans.property.BooleanProperty> entry : serviceSelections.entrySet()) {
                    if (entry.getValue().get()) {
                        newWorkOrder.addServiceItem(entry.getKey());
                    }
                }

                selectedBooking.setStatus("WORK_ORDER_CREATED");
                Database.getWorkOrders().add(newWorkOrder);

                // Rensa valen inför nästa gång
                serviceSelections.clear();
                messages.showSuccess("Work order created.");
                navigateToWorkOrderView();

            } else {
                messages.showError("You need to choose a booking and a mechanic!");
            }
        }
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
            WorkOrderController controller = loader.getController();
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


    public void setMessages(UserMessages messages) {
        this.messages = messages;
    }*/
}