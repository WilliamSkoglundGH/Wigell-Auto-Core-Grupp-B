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
                System.out.println("En workorder måste vara CREATED för att kunna startas");
            }
        } else {
            System.out.println("Välj en arbetsorder att starta först.");
        }
    }

    @FXML
    private void handleCompleteWorkOrder() {
        WorkOrder selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
        if (selected != null) {
            if ( "STARTED".equals(selected.getStatus()) || "CREATED".equals(selected.getStatus())) {
                selected.setStatus("COMPLETED");
                workOrderTable.refresh();
            } else if ("COMPLETED".equals(selected.getStatus())) {
                System.out.println("Workorder redan COMPLETED");
            } else {
                System.out.println("En workorder måste vara CREATED eller STARTED för att kunna bli COMPLETED");
            }
        } else {
            System.out.println("Välj en arbetsorder att slutföra först.");
        }
    }

    @FXML
    private void handleNewWorkOrder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/NewWorkOrderView.fxml"));
            loader.setController(this);
            Parent newWorkOrderView = loader.load();

            // Filtrera bokningar så att endast de med status "BOOKED" visas
            if (bookingComboBox != null) {
                List<Booking> bookedList = Database.getBookings().stream()
                        .filter(b -> "BOOKED".equalsIgnoreCase(b.getStatus())) // Förutsätter att Booking har getStatus()
                        .collect(Collectors.toList());

                bookingComboBox.setItems(FXCollections.observableArrayList(bookedList));
            }

            if (mechanicComboBox != null) {
                mechanicComboBox.setItems(FXCollections.observableArrayList(Database.getMechanics()));
            }

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(newWorkOrderView);
            } else {
                System.err.println("Kunde inte hitta BorderPane för att visa arbetsorder-formuläret!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Kunde inte ladda NewWorkOrderView.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveWorkOrder() {
        if (bookingComboBox != null && mechanicComboBox != null) {
            Booking selectedBooking = bookingComboBox.getValue();
            Mechanic selectedMechanic = mechanicComboBox.getValue();

            if (selectedBooking != null && selectedMechanic != null) {
                int newId = Database.getWorkOrders().size() + 1;

                // Skapar arbetsorder med ID från vald bokning och mekaniker
                WorkOrder newWorkOrder = new WorkOrder(newId, selectedBooking.getId(), selectedMechanic.getId());
                Database.getWorkOrders().add(newWorkOrder);

                navigateToWorkOrderView();
            } else {
                System.err.println("V både en bokning och en mekaniker!");
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
                System.err.println("Kunde inte hitta BorderPane för att återgå till arbetsordertabellen!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Kunde inte ladda tillbaka WorkOrderView.fxml: " + e.getMessage());
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