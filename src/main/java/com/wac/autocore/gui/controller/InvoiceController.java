package com.wac.autocore.gui.controller;

import com.wac.autocore.dto.workorder.WorkOrderSummaryDto;
import com.wac.autocore.dto.invoiceLine.InvoiceDto;
import com.wac.autocore.dto.invoiceLine.InvoiceLineDto;
import com.wac.autocore.exception.WorkOrderNotFoundException;
import com.wac.autocore.gui.util.FormatUIUtil;
import com.wac.autocore.model.InvoiceLine;
import com.wac.autocore.model.WorkOrderState;
import com.wac.autocore.service.InvoiceService;
import com.wac.autocore.service.WorkOrderService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.wac.autocore.model.Invoice;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("prototype")
public class InvoiceController extends OverController {

    //InvoiceView.fxml
    @FXML
    private TableView<Invoice> invoiceTable;
    @FXML
    private TableColumn<Invoice, Long> idColumn;
    @FXML
    private TableColumn<Invoice, Long> workOrderIdColumn;
    @FXML
    private TableColumn<Invoice, LocalDate> invoiceDateColumn;
    @FXML
    private TableColumn<Invoice, Double> amountColumn;
    @FXML
    private TableColumn<Invoice, Double> discountColumn;
    @FXML
    private TableColumn<Invoice, Double> totalAmountColumn;
    @FXML
    private TableColumn<Invoice, String> paidColumn;

    @FXML
    private TextField discountCodeField;
    @FXML
    private ComboBox<WorkOrderSummaryDto> workOrderComboBox;
    @FXML
    private Button detailsButton;

    //InvoiceDetailsView.fxml
    @FXML private Label idLabel;
    @FXML private Label workOrderLabel;
    @FXML private Label dateLabel;
    @FXML private Label amountLabel;
    @FXML private Label totalLabel;
    @FXML private Label discountLabel;
    @FXML private Label paidLabel;

    @FXML private TableView<InvoiceLineDto> linesTable;
    @FXML private TableColumn<InvoiceLineDto, String> lineNameOfServiceColumn;
    @FXML private TableColumn<InvoiceLineDto, BigDecimal> lineAmountBeforeColumn;
    @FXML private TableColumn<InvoiceLineDto, BigDecimal> lineDiscountColumn;
    @FXML private TableColumn<InvoiceLineDto, BigDecimal> lineTotalColumn;

    private static Long currentInvoiceId;

    private final InvoiceService invoiceService;
    private final WorkOrderService workOrderService;
    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

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
            paidColumn.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(
                            getString(cellData.getValue().isPaid() ? "common.yes" : "common.no")
                    ));
            //Ritar in funktion i tabellen, om man dubbelklickar kommer
            // man till detaljer eller om man markerar och trycker på knappen hänger radens info med.
            invoiceTable.setRowFactory(tv -> {
                TableRow<Invoice> row = new TableRow<>();
                row.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && !row.isEmpty()) {
                        currentInvoiceId = row.getItem().getId();
                        navigateToInvoiceDetailsView();
                    }
                });
                return row;
            });
            //Detaljer-knappen är disabled tills den rad är vald.
            detailsButton.disableProperty().bind(
                    invoiceTable.getSelectionModel().selectedItemProperty().isNull()
            );

            loadInvoiceData();
            invoiceTable.requestFocus();
        }

        // NewInvoiceView.fxml
        if (workOrderComboBox != null) {
            populateComboBox();
            workOrderComboBox.requestFocus();
        }
        if (idLabel != null && currentInvoiceId != null) {
            loadInvoiceDetails();
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

    //-----------------------------------
    // Invoice functions first page + modal
    //-----------------------------------


    @FXML
    private void handleDetails() {
        Invoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        currentInvoiceId = selected.getId();
        navigateToInvoiceDetailsView();
    }

    //-----------------------------------
    // Invoice Detail Modal
    //-----------------------------------

    public void loadInvoiceDetails(){

        if (messages != null) {
            messages.clearMessage();
        }

            //Hämta faktura och ladda info
            InvoiceDto invDTO = invoiceService.getInvoice(currentInvoiceId);
            idLabel.setText(String.valueOf(invDTO.getId()));
            workOrderLabel.setText(invDTO.getWorkOrderId() != null
                    ? String.valueOf(invDTO.getWorkOrderId()) : "Specialist");
            dateLabel.setText(FormatUIUtil.formatDate(invDTO.getInvoiceDate()));
            amountLabel.setText(String.valueOf(invDTO.getAmount()));
            totalLabel.setText(String.valueOf(invDTO.getTotalAmount()));
            discountLabel.setText(String.valueOf(invDTO.getDiscount()));
            paidLabel.setText(invDTO.isPaid()
                            ? "Paid"
                            : "Not paid");
            lineNameOfServiceColumn.setCellValueFactory(new PropertyValueFactory<>("nameOfService"));
            lineAmountBeforeColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            lineDiscountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
            lineTotalColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));
            linesTable.getItems().setAll(invDTO.getLines());


    }

    //-----------------------------------
    // New Invoice Functions
    //-----------------------------------

    private void populateComboBox() {
        if (workOrderComboBox != null) {
            List<Invoice> invoices = invoiceService.getAllInvoices();
            List<WorkOrderSummaryDto> workOrdersList = workOrderService.getAllWorkOrders().stream()
                    .filter(wo -> WorkOrderState.COMPLETED.equals(wo.getStatus())) // Endast färdiga
                    .filter(wo -> invoices.stream()
                            .noneMatch(inv -> inv.getWorkOrder().getId().equals(wo.getId()))) // Som INTE redan har en faktura
                    .collect(Collectors.toList());

            workOrderComboBox.setItems(FXCollections.observableArrayList(workOrdersList));
        }
        workOrderComboBox.setConverter(new StringConverter<WorkOrderSummaryDto>() {
            @Override
            public String toString(WorkOrderSummaryDto dto) {
                return getString("invoice.option.workorderId") + ": "
                        + dto.getId()
                        + " | " + getString("invoice.option.bookingId") + ": "
                        + dto.getBookingId();
            }

            @Override
            public WorkOrderSummaryDto fromString(String s) {
                return null;
            }
        });
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
        WorkOrderSummaryDto selectedWorkOrder = workOrderComboBox.getValue();
        String discountCode = discountCodeField != null ? discountCodeField.getText().trim() : "";

        try {
            invoiceService.createInvoice(selectedWorkOrder.getId(), discountCode);
        } catch (WorkOrderNotFoundException e) {
            messages.showError(getString("invoice.error.workorder_not_found"));
            return;
        } catch (IllegalStateException e) {
            messages.showError(getString(e.getMessage()));
            return;
        } catch (DataAccessException e) {
            logger.error("Could not create invoice", e);
            messages.showError(getString("invoice.error.create_invoice"));
            return;
        }

        String message = getString("invoice.success.invoice_created");
        //TODO Lägg om koder till enum
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
    private void handleBackToInvoiceView() {
        if (messages != null) {
            messages.clearMessage();
        }
        navigateToInvoiceView();
    }
    private void navigateToInvoiceDetailsView() {
        loadCenterView("/com/wac/autocore/gui/view/InvoiceDetailsView.fxml");

    }

    private void navigateToInvoiceView() {
        loadCenterView("/com/wac/autocore/gui/view/InvoiceView.fxml");
    }
}