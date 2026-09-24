package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Invoice;
import com.wac.autocore.model.Payment;
import com.wac.autocore.service.InvoiceService;
import com.wac.autocore.service.PaymentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the payment view. Fetches data directly from the database.
 */
@Controller
@Scope("prototype")
public class PaymentController extends OverController {

    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, Integer> idColumn;
    @FXML private TableColumn<Payment, Integer> invoiceIdColumn;
    @FXML private TableColumn<Payment, Double> amountColumn;
    @FXML private TableColumn<Payment, String> paymentTypeColumn;
    @FXML private TableColumn<Payment, LocalDateTime> paymentDateColumn;
    @FXML private TableColumn<Payment, Boolean> successfulColumn;

    @FXML private ComboBox<Invoice> invoiceComboBox;
    @FXML private ComboBox<String> paymentTypeComboBox;
    @FXML private TextField amountField;

    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    // Spring injicerar tjänster och ApplicationContext
    public PaymentController(PaymentService paymentService, InvoiceService invoiceService, ApplicationContext applicationContext) {
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        // PaymentView.fxml
        if (paymentTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            paymentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
            paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
            successfulColumn.setCellValueFactory(new PropertyValueFactory<>("successful"));

            loadPaymentData();
            paymentTable.requestFocus();
        }

        // NewPaymentView.fxml
        if (invoiceComboBox != null) {
            List<Invoice> unpaidInvoices = invoiceService.getAllInvoices().stream()
                    .filter(invoice -> !invoice.isPaid())
                    .collect(Collectors.toList());
            invoiceComboBox.setItems(FXCollections.observableArrayList(unpaidInvoices));
            invoiceComboBox.requestFocus();
        }

        if (paymentTypeComboBox != null) {
            paymentTypeComboBox.setItems(FXCollections.observableArrayList("CARD", "SWISH", "CASH"));
        }
    }

    public void loadPaymentData() {
        try {
            if (paymentTable != null) {
                ObservableList<Payment> paymentData = FXCollections.observableArrayList(paymentService.getAllPayments());
                paymentTable.setItems(paymentData);
            }
        } catch (Exception e) {
            logger.error("Could not load payments", e);
            if (messages != null) {
                messages.showError(getString("payment.error.could_not_load"));
            }
        }
    }
    @FXML
    private void handleNewPayment() {
        if (messages != null) {
            messages.clearMessage();
        }
        // Använder OverControllers gemensamma metod
        loadCenterView("/com/wac/autocore/gui/view/NewPaymentView.fxml");
    }

    @FXML
    private void handleSavePayment() {
        if (invoiceComboBox != null && paymentTypeComboBox != null) {
            Invoice selectedInvoice = invoiceComboBox.getValue();
            String paymentType = paymentTypeComboBox.getValue();

            if (selectedInvoice != null && paymentType != null && !paymentType.isEmpty()) {
                try {
                    // 1. Skicka över jobbet till servicen (Strategy-mönstret i bakgrunden)
                    String message = paymentService.processPayment(selectedInvoice.getId(), paymentType);

                    // 2. Visa snyggt meddelande
                    messages.showSuccess(message);

                    // 3. Navigera tillbaka till tabellen
                    navigateToPaymentView();

                } catch (Exception e) {
                    logger.error("Could not process payment: {}", e.getMessage(), e);
                    messages.showError(getString("payment.error.could_not_process"));
                }
            } else {
                messages.showError(getString("payment.error.select_invoice_type"));
            }
        }
    }

    @FXML
    private void handleCancel() {
        if (messages != null) {
            messages.clearMessage();
        }
        navigateToPaymentView();
    }

    private void navigateToPaymentView() {
        loadCenterView("/com/wac/autocore/gui/view/PaymentView.fxml");
    }
}