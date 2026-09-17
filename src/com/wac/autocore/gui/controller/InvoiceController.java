package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the invoice view. Fetches data directly from the database.
 */
public class InvoiceController {

    @FXML
    private TableView<Invoice> invoiceTable;
    @FXML
    private TableColumn<Invoice, Integer> idColumn;
    @FXML
    private TableColumn<Invoice, Integer> workOrderIdColumn;
    @FXML
    private TableColumn<Invoice, LocalDate> invoiceDateColumn;
    @FXML
    private TableColumn<Invoice, Double> amountColumn;
    @FXML
    private TableColumn<Invoice, Double> discountColumn;
    @FXML
    private TableColumn<Invoice, Double> totalAmountColumn;
    @FXML
    private TableColumn<Invoice, Boolean> paidColumn;

    @FXML private ComboBox<Integer> discountComboBox;
    @FXML private ComboBox<WorkOrder> workOrderComboBox;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        workOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("workOrderId"));
        invoiceDateColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        paidColumn.setCellValueFactory(new PropertyValueFactory<>("paid"));

        loadInvoiceData();
    }

    public void loadInvoiceData() {
        ObservableList<Invoice> invoiceData = FXCollections.observableArrayList(
                Database.getInvoices()
        );

        invoiceTable.setItems(invoiceData);
    }

    private BorderPane findMainLayout() {
        Scene scene = null;

        if (workOrderComboBox != null && workOrderComboBox.getScene() != null) {
            scene = workOrderComboBox.getScene();
        } else if (invoiceTable != null && invoiceTable.getScene() != null) {
            scene = invoiceTable.getScene();
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

    private void populateComboBox() {
        if (workOrderComboBox != null) {
            List<WorkOrder> workOrdersList = Database.getWorkOrders().stream()
                    .filter(b -> "COMPLETED".equalsIgnoreCase(b.getStatus()))
                    .collect(Collectors.toList());
            workOrderComboBox.setItems(FXCollections.observableArrayList(workOrdersList));
        }
        // Fyll rabatt-comboboxen med siffror 0-100
        if (discountComboBox != null) {
            List<Integer> numbers = java.util.stream.IntStream.rangeClosed(0, 100)
                    .boxed()
                    .collect(Collectors.toList());

            discountComboBox.setItems(FXCollections.observableArrayList(numbers));
            discountComboBox.getSelectionModel().select(Integer.valueOf(0)); // 0 blir förvalt
        }
    }

    @FXML
    private void handleNewInvoice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/NewInvoiceView.fxml"));
            loader.setController(this);
            Parent newInvoiceView = loader.load();

            populateComboBox();

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(newInvoiceView);

            } else {
                System.err.println("Could not find BorderPane to present the form.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load NewInvoiceView.fxml: " + e.getMessage());
        }
    }
    @FXML
    private void handleSaveInvoice() {
        // 1. Säkerhet: Kontrollera att en arbetsorder faktiskt är vald
        if (workOrderComboBox == null || workOrderComboBox.getValue() == null) {
            System.err.println("Please select a work order before saving an invoice!");
            return;
        }

        WorkOrder workOrder = workOrderComboBox.getValue();

        // 2. Säkerhet: Förhindra dubblettfakturor för samma arbetsorder
        boolean alreadyInvoiced = Database.getInvoices().stream()
                .anyMatch(i -> i.getWorkOrderId() == workOrder.getId());

        if (alreadyInvoiced) {
            System.err.println("An invoice already exists for this work order!");
            return;
        }

        // 3. Beräkna totalt belopp från arbetsorderns service items FÖRST
        double amount = 0.0;
        if (workOrder.getServiceItemIds() != null) {
            for (Integer serviceItemId : workOrder.getServiceItemIds()) {
                ServiceItem serviceItem = Database.getServiceItems().stream()
                        .filter(s -> s.getId() == serviceItemId)
                        .findFirst()
                        .orElse(null);

                if (serviceItem != null) {
                    amount += serviceItem.getPrice();
                }
            }
        }

        // 4. Kontrollera om kunden är VIP för att applicera rabatt
        double discount = 0.0;
        Booking thisBooking = Database.getBookings().stream()
                .filter(b -> b.getId() == workOrder.getBookingId())
                .findFirst()
                .orElse(null);

        if (thisBooking != null) {
            Vehicle vehicle = Database.getVehicles().stream()
                    .filter(v -> v.getId() == thisBooking.getVehicleId())
                    .findFirst()
                    .orElse(null);

            if (vehicle != null) {
                Customer customer = Database.getCustomers().stream()
                        .filter(c -> c.getId() == vehicle.getCustomerId())
                        .findFirst()
                        .orElse(null);

                if (customer != null && customer.isVip()) {
                    discount = amount * 0.10; // 10% VIP-rabatt
                    System.out.println("VIP discount applied: 10%");
                }
            }
        }

        // 5. Hantera extra rabatt från ComboBox (0-100%)
        double manualDiscount = 0.0;
        if (discountComboBox != null && discountComboBox.getValue() != null) {
            int selectedValue = discountComboBox.getValue();
            manualDiscount = amount * (selectedValue / 100.0);
        }

        // Addera till den totala rabatten
        discount += manualDiscount;
        double totalAmount = amount - discount;
        LocalDate invoiceDate = LocalDate.now();
        int newId = Database.getInvoices().size() + 1;

        // 6. Skapa och spara fakturan
        Invoice newInvoice = new Invoice(newId, workOrder.getId(), invoiceDate, amount);

        if (discount > 0) {
            newInvoice.setDiscount(discount);
        }

        Database.getInvoices().add(newInvoice);

        // 7. Gå tillbaka till fakturavy-tabellen
        navigateToInvoiceView();
    }
    @FXML
    private void handleCancel() {
        navigateToInvoiceView();
    }

    private void navigateToInvoiceView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/InvoiceView.fxml"));
            Parent invoiceView = loader.load();

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(invoiceView);
            } else {
                System.err.println("Could not find BorderPane!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load InvoiceView.fxml: " + e.getMessage());
        }
    }
}