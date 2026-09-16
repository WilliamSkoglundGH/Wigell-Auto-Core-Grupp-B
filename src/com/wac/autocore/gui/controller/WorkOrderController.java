package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.WorkOrder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class WorkOrderController {

    @FXML private TableView<WorkOrder> workOrderTable;
    @FXML private TableColumn<WorkOrder, Integer> idColumn;
    @FXML private TableColumn<WorkOrder, Integer> bookingIdColumn;
    @FXML private TableColumn<WorkOrder, Integer> mechanicIdColumn;
    @FXML private TableColumn<WorkOrder, String> statusColumn;

    @FXML private TextField bookingIdField;
    @FXML private TextField mechanicIdField;

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
            selected.setStatus("STARTED");
            workOrderTable.refresh(); // Uppdaterar tabellen så den nya statusen syns direkt
        } else {
            System.out.println("Välj en arbetsorder att starta först.");
        }
    }

    @FXML
    private void handleCompleteWorkOrder() {
        WorkOrder selected = workOrderTable != null ? workOrderTable.getSelectionModel().getSelectedItem() : null;
        if (selected != null) {
            selected.setStatus("COMPLETED");
            workOrderTable.refresh();
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
        if (bookingIdField != null && mechanicIdField != null) {
            try {
                int bookingId = Integer.parseInt(bookingIdField.getText().trim());
                int mechanicId = Integer.parseInt(mechanicIdField.getText().trim());

                int newId = Database.getWorkOrders().size() + 1;

                // Använder din exakta konstruktör: WorkOrder(int id, int bookingId, int mechanicId)
                WorkOrder newWorkOrder = new WorkOrder(newId, bookingId, mechanicId);
                Database.getWorkOrders().add(newWorkOrder);

                navigateToWorkOrderView();
            } catch (NumberFormatException e) {
                System.err.println("Booking ID och Mechanic ID måste vara giltiga heltal!");
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

        if (bookingIdField != null && bookingIdField.getScene() != null) {
            scene = bookingIdField.getScene();
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