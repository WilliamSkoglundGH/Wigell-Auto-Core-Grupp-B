package com.wac.autocore.controller.javafx;

import com.wac.autocore.dto.invoice.InvoiceSummaryDto;
import com.wac.autocore.dto.payment.PaymentSummaryDto;
import com.wac.autocore.exception.BusinessException;
import com.wac.autocore.controller.util.FormatUIUtil;
import com.wac.autocore.exception.DatabaseException;
import com.wac.autocore.service.InvoiceService;
import com.wac.autocore.service.PaymentService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the payment view. Fetches data via DTOs.
 */
@Controller
@Scope("prototype")
public class PaymentController extends OverController {

    @FXML private TableView<PaymentSummaryDto> paymentTable;
    @FXML private TableColumn<PaymentSummaryDto, Long> idColumn;
    @FXML private TableColumn<PaymentSummaryDto, Long> invoiceIdColumn;
    @FXML private TableColumn<PaymentSummaryDto, Double> amountColumn;
    @FXML private TableColumn<PaymentSummaryDto, String> paymentTypeColumn;
    @FXML private TableColumn<PaymentSummaryDto, String> paymentDateColumn;
    @FXML private TableColumn<PaymentSummaryDto, String> successfulColumn;

    @FXML private ComboBox<InvoiceSummaryDto> invoiceComboBox;
    @FXML private ComboBox<String> paymentTypeComboBox;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

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
            invoiceIdColumn.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(
                            cellData.getValue().getInvoiceId()
                    ));
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            paymentTypeColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            getString("payment.type." + cellData.getValue().getPaymentType())
                    )
            );
            paymentDateColumn.setCellValueFactory(cellData -> {
                PaymentSummaryDto pay = cellData.getValue();
                return new SimpleStringProperty(FormatUIUtil.formatTime(pay.getPaymentDate()));
            });
            successfulColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            getString(cellData.getValue().isSuccessful()
                                    ? "common.yes"
                                    : "common.no"))
            );

            loadPaymentData();
            paymentTable.requestFocus();
        }

        // NewPaymentView.fxml
        if (invoiceComboBox != null) {
            try {
                List<InvoiceSummaryDto> unpaidInvoices = invoiceService.getAllInvoices().stream()
                        .filter(invoice -> !invoice.isPaid())
                        .collect(Collectors.toList());
                invoiceComboBox.setItems(FXCollections.observableArrayList(unpaidInvoices));
            } catch (DatabaseException e) {
                if (messages != null) {
                    messages.showError(getString(e.getMessage()));
                }
            } catch (Exception e) {
                logger.error("Method: initialize | Could not load invoices", e);
                if (messages != null) {
                    messages.showError(getString("payment.error.could_not_load"));
                }
            }
            invoiceComboBox.setConverter(new StringConverter<InvoiceSummaryDto>() {
                @Override
                public String toString(InvoiceSummaryDto invoice) {
                    if (invoice == null) {
                        return "";
                    }
                    return getString("payment.option.invoice") + ": "
                            + invoice.getId()
                            + " | " + getString("payment.option.workorder") + ": "
                            + invoice.getWorkOrderId()
                            + " | " + getString("payment.option.invoiceDate") + ": "
                            + invoice.getInvoiceDate()
                            + " | " + getString("payment.option.amountDue") + ": "
                            + invoice.getTotalAmount();
                }

                @Override
                public InvoiceSummaryDto fromString(String text) {
                    return null;
                }
            });
            invoiceComboBox.requestFocus();
        }

        if (paymentTypeComboBox != null) {
            // Skicka med råa koder, StringConverter sköter översättningen
            paymentTypeComboBox.setItems(FXCollections.observableArrayList("CARD", "SWISH", "CASH"));

            paymentTypeComboBox.setConverter(new StringConverter<String>() {
                @Override
                public String toString(String paymentType) {
                    if (paymentType == null) {
                        return "";
                    }
                    return getString("payment.type." + paymentType);
                }

                @Override
                public String fromString(String text) {
                    return null;
                }
            });
        }
    }

    public void loadPaymentData() {
        try {
            if (paymentTable != null) {
                ObservableList<PaymentSummaryDto> paymentData = FXCollections.observableArrayList(paymentService.getAllPayments());
                paymentTable.setItems(paymentData);
            }
        } catch (DatabaseException e) {
            if (messages != null) {
                messages.showError(getString(e.getMessage()));
            }
        } catch (Exception e) {
            logger.error("Method: loadPaymentData | Could not load payments", e);
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
        loadCenterView("/com/wac/autocore/controller/view/NewPaymentView.fxml");
    }

    @FXML
    private void handleSavePayment() {
        if (invoiceComboBox != null && paymentTypeComboBox != null) {
            InvoiceSummaryDto selectedInvoice = invoiceComboBox.getValue();
            String paymentType = paymentTypeComboBox.getValue();

            if (selectedInvoice != null && paymentType != null && !paymentType.isEmpty()) {
                try {
                    String messageKey = paymentService.processPayment(selectedInvoice.getId(), paymentType);

                    String message = String.format(
                            getString(messageKey),
                            selectedInvoice.getId()
                    );

                    messages.showSuccess(message);
                    navigateToPaymentView();

                } catch (BusinessException | DatabaseException e) {
                    messages.showError(getString(e.getMessage()));
                } catch (Exception e) {
                    logger.error("Method: handleSavePayment | Could not process payment | InvoiceId={} | Type={}",
                            selectedInvoice.getId(), paymentType, e);
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
        loadCenterView("/com/wac/autocore/controller/view/PaymentView.fxml");
    }
}