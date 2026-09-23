package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.gui.launcher.GarageServiceBridge;
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
import com.wac.autocore.model.Invoice;
import com.wac.autocore.model.WorkOrder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the invoice view. Fetches data directly from the database.
 */
public class InvoiceController extends Controller{

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

    @FXML
    private TextField discountCodeField;
    @FXML
    private ComboBox<WorkOrder> workOrderComboBox;

    private UserMessages messages;
    
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



    private void populateComboBox() {
        if (workOrderComboBox != null) {
            List<WorkOrder> workOrdersList = Database.getWorkOrders().stream()
                    .filter(wo -> "COMPLETED".equalsIgnoreCase(wo.getStatus())) // Endast färdiga
                    .filter(wo -> Database.getInvoices().stream()
                            .noneMatch(inv -> inv.getWorkOrder().getId() == wo.getId())) // Som INTE redan har en faktura
                    .collect(Collectors.toList());

            workOrderComboBox.setItems(FXCollections.observableArrayList(workOrdersList));
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
                workOrderComboBox.requestFocus();

            } else {
                messages.showError("Could not find BorderPane to present the form.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            messages.showError("Could not load NewInvoiceView.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveInvoice() {
        // 1. Säkerhet: Kontrollera att en arbetsorder faktiskt är vald
        if (workOrderComboBox == null || workOrderComboBox.getValue() == null) {
            messages.showError("Please select a work order before saving an invoice!");
            return;
        }

        WorkOrder workOrder = workOrderComboBox.getValue();

        // 2. Säkerhet: Förhindra dubblettfakturor för samma arbetsorder
        boolean alreadyInvoiced = Database.getInvoices().stream()
                .anyMatch(i -> i.getWorkOrder().getId() == workOrder.getId());

        if (alreadyInvoiced) {
            messages.showError("An invoice already exists for this work order!");
            return;
        }

        String discountCode = discountCodeField.getText().trim();

        Invoice invoice = GarageServiceBridge.getInstance()
                .createInvoice(workOrder.getId(), discountCode);

        if (invoice != null) {
            String message = "Invoice created.";

            if ("WELCOME10".equalsIgnoreCase(discountCode)) {
                message += " Discount code WELCOME10 applied.";
            } else if ("SERVICE200".equalsIgnoreCase(discountCode)) {
                message += " Discount code SERVICE200 applied.";
            } else if (!discountCode.isEmpty()) {
                message += " Unknown discount code.";
            }

            messages.showSuccess(message);
            navigateToInvoiceView();
        } else {
            messages.showError("Invoice not created.");
        }


    }

    @FXML
    private void handleCancel() {
        navigateToInvoiceView();
    }

    private void navigateToInvoiceView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/InvoiceView.fxml"));
            Parent invoiceView = loader.load();

            // Hämta den nya kontrollern och skicka med messages!
            InvoiceController controller = loader.getController();
            if (controller != null) {
                controller.setMessages(this.messages);
            }

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(invoiceView);
                controller.invoiceTable.requestFocus();
            } else {
                if (messages != null) {
                    messages.showError("Could not find BorderPane!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (messages != null) {
                messages.showError("Could not load InvoiceView.fxml: " + e.getMessage());
            }
        }
    }

    public void setMessages(UserMessages messages) {
        this.messages = messages;
    }
}