package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Invoice;
import com.wac.autocore.model.Payment;
import com.wac.autocore.service.InvoiceService;
import com.wac.autocore.service.PaymentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the payment view. Fetches data directly from the database.
 */
@Controller
public class PaymentController extends OverController {

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
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    public PaymentController(PaymentService paymentService, InvoiceService invoiceService) {
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
    }

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
            ObservableList<Payment> paymentData = FXCollections.observableArrayList(paymentService.getAllPayments());
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
                List<Invoice> unpaidInvoices = invoiceService.getAllInvoices().stream()
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
                try {
                    // 1. Skicka över jobbet till servicen! Det är här Strategy-mönstret används i bakgrunden.
                    paymentService.processPayment(selectedInvoice.getId(), paymentType);

                    // 2. Visa snyggt meddelande beroende på betalsätt
                    messages.showSuccess(paymentType.toLowerCase() + ": Payment completed successfully.");

                    // 3. Navigera tillbaka till tabellen
                    navigateToPaymentView();

                } catch (Exception e) {
                    logger.error("Could not process payment: {}", e.getMessage(), e);
                    messages.showError("Could not process payment: " + e.getMessage());
                }
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

    public void setMessages(UserMessages messages) {
        this.messages = messages;
    }
}
