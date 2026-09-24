package com.wac.autocore.gui.controller;

import com.wac.autocore.exception.WorkOrderNotFoundException;
import com.wac.autocore.service.InvoiceService;
import com.wac.autocore.service.WorkOrderService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import com.wac.autocore.model.Invoice;
import com.wac.autocore.model.WorkOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class InvoiceController extends OverController {

    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, Long> idColumn;
    @FXML private TableColumn<Invoice, Long> workOrderIdColumn;
    @FXML private TableColumn<Invoice, LocalDate> invoiceDateColumn;
    @FXML private TableColumn<Invoice, Double> amountColumn;
    @FXML private TableColumn<Invoice, Double> discountColumn;
    @FXML private TableColumn<Invoice, Double> totalAmountColumn;
    @FXML private TableColumn<Invoice, Boolean> paidColumn;

    @FXML private TextField discountCodeField;
    @FXML private ComboBox<WorkOrder> workOrderComboBox;

    private final InvoiceService invoiceService;
    private final WorkOrderService workOrderService;
    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    // Spring injicerar tjänster samt ApplicationContext (som sätts via superklassen)
    public InvoiceController(InvoiceService invoiceService, WorkOrderService workOrderService, ApplicationContext applicationContext) {
        this.invoiceService = invoiceService;
        this.workOrderService = workOrderService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        // InvoiceView.fxml
        if (invoiceTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            workOrderIdColumn.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(
                            cellData.getValue().getWorkOrder().getId()
                    ));
            invoiceDateColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
            totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
            paidColumn.setCellValueFactory(new PropertyValueFactory<>("paid"));

            loadInvoiceData();
            invoiceTable.requestFocus();
        }

        // NewInvoiceView.fxml
        if (workOrderComboBox != null) {
            populateComboBox();
            workOrderComboBox.requestFocus();
        }
    }

    public void loadInvoiceData() {
        try {
            ObservableList<Invoice> invoiceData = FXCollections.observableArrayList(
                    invoiceService.getAllInvoices());
            invoiceTable.setItems(invoiceData);
        } catch (DataAccessException e) {
            logger.error("Could not load invoices", e);
            if (messages != null) {
                messages.showError(getString("invoice.error.load_invoices"));
            }
        }
    }

    private void populateComboBox() {
        if (workOrderComboBox != null) {
            List<WorkOrder> workOrdersList = workOrderService.getAllWorkOrders().stream()
                    .filter(wo -> "COMPLETED".equalsIgnoreCase(wo.getStatus())) // Endast färdiga
                    .filter(wo -> invoiceService.getAllInvoices().stream()
                            .noneMatch(inv -> inv.getWorkOrder().getId().equals(wo.getId()))) // Som INTE redan har en faktura
                    .collect(Collectors.toList());

            workOrderComboBox.setItems(FXCollections.observableArrayList(workOrdersList));
        }
    }

    @FXML
    private void handleNewInvoice() {
        if (messages != null) {
            messages.clearMessage();
        }
        // Använder basklassens vyladdare – språket och Spring-kontexten följer med
        loadCenterView("/com/wac/autocore/gui/view/NewInvoiceView.fxml");
    }

    @FXML
    private void handleSaveInvoice() {
        if (workOrderComboBox == null || workOrderComboBox.getValue() == null) {
            messages.showError(getString("invoice.error.select_workorder"));
            return;
        }

        WorkOrder selectedWorkOrder = workOrderComboBox.getValue();
        String discountCode = discountCodeField != null ? discountCodeField.getText().trim() : "";

        try {
            invoiceService.createInvoice(selectedWorkOrder.getId(), discountCode);
        } catch (WorkOrderNotFoundException | IllegalStateException e) {
            messages.showError(e.getMessage());
            return;
        } catch (DataAccessException e) {
            logger.error("Could not create invoice", e);
            messages.showError(getString("invoice.error.create_invoice"));
            return;
        }

        String message = getString("invoice.success.invoice_created");

        if (discountCode.equalsIgnoreCase("WELCOME10")) {
            message += getString("invoice.success.welcome10");
        } else if (discountCode.equalsIgnoreCase("SERVICE200")) {
            message += getString("invoice.success.service200");
        } else if (!discountCode.isEmpty()) {
            message += getString("invoice.success.unknown_discount");
        }
        messages.showSuccess(message);
        navigateToInvoiceView();
    }

    @FXML
    private void handleCancel() {
        if (messages != null) {
            messages.clearMessage();
        }
        navigateToInvoiceView();
    }

    private void navigateToInvoiceView() {
        loadCenterView("/com/wac/autocore/gui/view/InvoiceView.fxml");
    }
}