package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.WorkOrder;
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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class WorkOrderController {

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
    private final java.util.Map<Integer, javafx.beans.property.BooleanProperty> serviceSelections = new java.util.HashMap<>();

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

    private BorderPane searchBorderPaneRecursive(Parent parent) {
        if (parent instanceof BorderPane) {
            return (BorderPane) parent;
        }
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                BorderPane found = searchBorderPaneRecursive((Parent) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public void setMessages(UserMessages messages) {
        this.messages = messages;
    }
}