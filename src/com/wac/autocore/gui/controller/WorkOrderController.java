package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.model.WorkOrder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
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

    @FXML private ComboBox<Booking> bookingComboBox;
    @FXML private ComboBox<Mechanic> mechanicComboBox;

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
            if ("CREATED".equals(selected.getStatus())) {
                selected.setStatus("STARTED");
                workOrderTable.refresh();
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
            if ("STARTED".equals(selected.getStatus()) || "CREATED".equals(selected.getStatus())) {
                selected.setStatus("COMPLETED");
                workOrderTable.refresh();
            } else if ("COMPLETED".equals(selected.getStatus())) {
                System.out.println("Workorder already COMPLETED");
            } else {
                System.out.println("A workorder must be CREATED or STARTED to be COMPLETED");
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

            // Fyll ComboBoxarna EFTER att loopen/laddningen är klar
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
    }

    @FXML
    private void handleSaveWorkOrder() {
        if (bookingComboBox != null && mechanicComboBox != null) {
            Booking selectedBooking = bookingComboBox.getValue();
            Mechanic selectedMechanic = mechanicComboBox.getValue();

            if (selectedBooking != null && selectedMechanic != null) {
                int newId = Database.getWorkOrders().size() + 1;

                WorkOrder newWorkOrder = new WorkOrder(newId, selectedBooking.getId(), selectedMechanic.getId());
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