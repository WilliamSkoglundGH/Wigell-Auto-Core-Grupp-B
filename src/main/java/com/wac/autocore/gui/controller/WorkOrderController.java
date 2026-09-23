package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.WorkOrder;
import com.wac.autocore.service.BookingService;
import com.wac.autocore.service.MechanicService;
import com.wac.autocore.service.ServiceItemService;
import com.wac.autocore.service.WorkOrderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class WorkOrderController extends Controller{

    @FXML private TableColumn estimatedTimeColumn;
    @FXML private TableView<WorkOrder> workOrderTable;
    @FXML private TableColumn<WorkOrder, Integer> idColumn;
    @FXML private TableColumn<WorkOrder, Integer> bookingIdColumn;
    @FXML private TableColumn<WorkOrder, Integer> mechanicIdColumn;
    @FXML private TableColumn<WorkOrder, String> statusColumn;
    @FXML private TableColumn<WorkOrder, String> servicesColumn;

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
    private static final Logger logger = LoggerFactory.getLogger(WorkOrderController.class);

    public WorkOrderController(BookingService bookingService, MechanicService mechanicService, ServiceItemService serviceItemService, WorkOrderService workOrderService) {
        this.bookingService = bookingService;
        this.mechanicService = mechanicService;
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
            if (servicesColumn != null) {
                servicesColumn.setCellValueFactory(cellData -> {
                    WorkOrder wo = cellData.getValue();
                    if (wo.getServiceItems() == null || wo.getServiceItems().isEmpty()) {
                        return new javafx.beans.property.SimpleStringProperty("No services");
                    }

                    // Skapa en sträng med "ID: Namn" för varje tjänst
                    String serviceInfo = wo.getServiceItems().stream()
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
                    workOrderService.getAllWorkOrders()
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


                Mechanic mechanic = mechanicService.getAllMechanics().stream() // Hoppa över stream och lägg direkt mot id vi har ju id??
                        .filter(m -> m.getId() == selected.getId())
                        .findFirst()
                        .orElse(null);
                if (mechanic != null) {
                    mechanic.setAvailable(false); // Sätt mekanikern som otillgänglig mot databasen också !!!
                }
                Booking booking = bookingService.getAllBookings().stream()
                        .filter(b -> b.getId() == selected.getId())
                        .findFirst()
                        .orElse(null);
                if (booking != null) {
                    booking.setStatus("IN_PROGRESS"); // Spara Booking mot databasen också.
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
                Mechanic mechanic = mechanicService.getAllMechanics().stream()
                        .filter(m -> m.getId() == selected.getId())
                        .findFirst()
                        .orElse(null);
                if (mechanic != null) {
                    mechanic.setAvailable(true); // Sätt mekanikern som otillgänglig mot databasen också !!!
                }

                // 2. Uppdatera bokningens status till COMPLETED- Spara över via booking??
                Booking booking = bookingService.getAllBookings().stream()
                        .filter(b -> b.getId() == selected.getId())
                        .findFirst()
                        .orElse(null);
                if (booking != null) {
                    booking.setStatus("COMPLETED");  // Spara Booking mot databasen också. VILKA SPARNINGAR ÄRVS?? NU NÄR MECANIC ÄR I BOOKING??
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
                serviceSelections.putIfAbsent(item.getId(), new javafx.beans.property.SimpleBooleanProperty(false));
            }

            // Använd JavaFXs inbyggda CheckBoxListCell som hanterar cell-återanvändning galant
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
        try{
        if (bookingComboBox != null && mechanicComboBox != null) {
            Booking selectedBooking = bookingComboBox.getValue();
            Mechanic selectedMechanic = mechanicComboBox.getValue();

            if (selectedBooking != null && selectedMechanic != null) {
                //int newId = Database.getWorkOrders().size() + 1;

                // Hämta alla tjänster som markerats i mapen
                for (java.util.Map.Entry<Long, javafx.beans.property.BooleanProperty> entry : serviceSelections.entrySet()) {
                    if (entry.getValue().get()) {
                        newWorkOrder.addServiceItem(entry.getKey()); // Hämta id och plocka serviceId genom workorderservice metoden eller här??
                    }
                // Skapa arbetsordern
                //Behöver man en workorderkonstruktor utan serviceitems för att få lägga in dem efter skapande?
                //Metod för att ta nycklar från map och göra om till serviceItem för att lägga i listan på workorders.
                WorkOrder newWorkOrder = new WorkOrder(selectedBooking.getId(), selectedMechanic.getId());

                }
                workOrderService.saveWorkOrder();

                selectedBooking.setStatus("WORK_ORDER_CREATED");


                // Rensa valen inför nästa gång
                serviceSelections.clear();
                messages.showSuccess("Work order created.");
                navigateToWorkOrderView();

            } else {
                messages.showError("You need to choose a booking and a mechanic!");
            }
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
    }
}