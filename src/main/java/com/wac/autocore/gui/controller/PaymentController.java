package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.Invoice;
import com.wac.autocore.model.Payment;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the payment view. Fetches data directly from the database.
 */
public class PaymentController {

    @FXML
    private TableView<Payment> paymentTable;
    @FXML
    private TableColumn<Payment, Integer> idColumn;
    @FXML
    private TableColumn<Payment, Integer> invoiceIdColumn;
    @FXML
    private TableColumn<Payment, Double> amountColumn;
    @FXML
    private TableColumn<Payment, String> paymentTypeColumn;
    @FXML
    private TableColumn<Payment, LocalDateTime> paymentDateColumn;
    @FXML
    private TableColumn<Payment, Boolean> successfulColumn;
    @FXML
    private ComboBox<Invoice> invoiceComboBox;
    @FXML
    private ComboBox<String> paymentTypeComboBox;
    @FXML
    private TextField amountField;

    private UserMessages messages;

    @FXML
    public void initialize() {
        if (paymentTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            paymentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
            paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
            successfulColumn.setCellValueFactory(new PropertyValueFactory<>("successful"));

            loadPaymentData();
        }
    }

    public void loadPaymentData() {
        if (paymentTable != null) {
            ObservableList<Payment> paymentData = FXCollections.observableArrayList(
                    Database.getPayments()
            );
            paymentTable.setItems(paymentData);
        }
    }

    @FXML
    private void handleNewPayment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/NewPaymentView.fxml"));
            loader.setController(this);
            Parent newPaymentView = loader.load();

            if (invoiceComboBox != null) {
                List<Invoice> unpaidInvoices = Database.getInvoices().stream()
                        .filter(invoice -> !invoice.isPaid())
                        .collect(Collectors.toList());
                invoiceComboBox.setItems(FXCollections.observableArrayList(unpaidInvoices));
            }

            if (paymentTypeComboBox != null) {
                paymentTypeComboBox.setItems(FXCollections.observableArrayList("CARD", "SWISH", "CASH"));
            }

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(newPaymentView);
                invoiceComboBox.requestFocus();
            } else {
                messages.showError("Could not find BorderPane!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            messages.showError("Could not load NewPaymentView.fxml: " + e.getMessage());
        }
    }
    @FXML
    private void handleSavePayment() {
        if (invoiceComboBox != null && paymentTypeComboBox != null) {
            Invoice selectedInvoice = invoiceComboBox.getValue();
            String paymentType = paymentTypeComboBox.getValue();

            if (selectedInvoice != null && paymentType != null && !paymentType.isEmpty()) {
                int newId = Database.getPayments().size() + 1;

                // ÄNDRA HÄR: Använd getTotalAmount() istället för getAmount()
                double amount = selectedInvoice.getTotalAmount();

                Payment newPayment = new Payment(newId, selectedInvoice.getId(), amount, paymentType);
                newPayment.setSuccessful(true);
                Database.getPayments().add(newPayment);

                selectedInvoice.setPaid(true);


                if ("CARD".equalsIgnoreCase(paymentType)) {
                    messages.showSuccess("card: Payment completed successfully.");
                } else if ("CASH".equalsIgnoreCase(paymentType)) {
                    messages.showSuccess("cash: : Payment completed successfully.");
                } else {
                    messages.showSuccess("swish: Payment completed successfully.");
                }
                navigateToPaymentView();
            } else {
                messages.showError("Please select an invoice and payment type!");
            }
        }
    }

    @FXML
    private void handleCancel() {
        navigateToPaymentView();
    }

    private void navigateToPaymentView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/PaymentView.fxml"));
            Parent paymentView = loader.load();

            // Hämta den nya kontrollern och skicka med messages!
            PaymentController controller = loader.getController();
            if (controller != null) {
                controller.setMessages(this.messages);
            }

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(paymentView);
                controller.paymentTable.requestFocus();
            } else {
                if (messages != null) {
                    messages.showError("Could not find BorderPane!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (messages != null) {
                messages.showError("Could not load PaymentView.fxml: " + e.getMessage());
            }
        }
    }

    private BorderPane findMainLayout() {
        Scene scene = null;

        if (invoiceComboBox != null && invoiceComboBox.getScene() != null) {
            scene = invoiceComboBox.getScene();
        } else if (paymentTable != null && paymentTable.getScene() != null) {
            scene = paymentTable.getScene();
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
/*



Calling Swish payment service...
Swish payment approved.
Payment completed successfully.
Sending payment confirmation to customer...
Confirmation sent.



 */