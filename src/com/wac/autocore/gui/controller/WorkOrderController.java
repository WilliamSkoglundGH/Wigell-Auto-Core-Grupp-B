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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxListCell;
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

    @FXML private ComboBox<Booking> bookingComboBox;
    @FXML private ComboBox<Mechanic> mechanicComboBox;
    @FXML private ListView<ServiceItem> servicesListView; // Kopplad till FXML

    @FXML
    public void initialize() {
        if (workOrderTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
            mechanicIdColumn.setCellValueFactory(new PropertyValueFactory<>("mechanicId"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

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
                System.out.println("Work order " + selected.getId() + " has been started.");
            } else {
                System.out.println("A workorder must have status CREATED to be started.");
            }
        } else {
            System.out.println("Please choose a workorder to start.");
        }
    }

    @FXML
    private void handleCompleteWorkOrder() {
        WorkOrder selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
        if (selected != null) {
            if ("IN_PROGRESS".equals(selected.getStatus()) || "CREATED".equals(selected.getStatus())) {
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
                System.out.println("Work order " + selected.getId() + " has been completed.");
            } else if ("COMPLETED".equals(selected.getStatus())) {
                System.out.println("Workorder already COMPLETED");
            } else {
                System.out.println("A workorder must be CREATED or IN_PROGRESS to be COMPLETED");
            }
        } else {
            System.out.println("Please choose a workorder to complete.");
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
            } else {
                System.err.println("Could not find BorderPane!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load NewWorkOrderView.fxml: " + e.getMessage());
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
            // Hämta alla tjänster som en ObservableList
            ObservableList<ServiceItem> serviceItems = FXCollections.observableArrayList(Database.getServiceItems());
            servicesListView.setItems(serviceItems);

            // Gör om raderna till Checkboxar
            servicesListView.setCellFactory(CheckBoxListCell.forListView(item -> {
                // Skapa en boolean-egenskap för varje item som håller koll på om den är markerad
                javafx.beans.property.BooleanProperty observable = new javafx.beans.property.SimpleBooleanProperty();

                // Lyssna på när användaren kryssar i/ur och lägg till/ta bort från urvalet
                observable.addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        servicesListView.getSelectionModel().select(item);
                    } else {
                        servicesListView.getSelectionModel().clearSelection(servicesListView.getItems().indexOf(item));
                    }
                });
                return observable;
            }));
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

                // Hämta markerade tjänster från ListView och lägg till dem
                if (servicesListView != null) {
                    List<ServiceItem> selectedServices = servicesListView.getSelectionModel().getSelectedItems();
                    for (ServiceItem item : selectedServices) {
                        newWorkOrder.addServiceItem(item.getId());
                    }
                }
                selectedBooking.setStatus("WORK_ORDER_CREATED");
                Database.getWorkOrders().add(newWorkOrder);

                navigateToWorkOrderView();
            } else {
                System.err.println("You need to choose a booking and a mechanic!");
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

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(workOrderView);
            } else {
                System.err.println("Could not find BorderPane!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load WorkOrderView.fxml: " + e.getMessage());
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
}