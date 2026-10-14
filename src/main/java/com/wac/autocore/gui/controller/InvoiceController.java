package com.wac.autocore.gui.controller;

import com.wac.autocore.dto.invoice.InvoiceSummaryDto;
import com.wac.autocore.dto.invoice.InvoiceDetailsDto;
import com.wac.autocore.dto.invoice.InvoiceCreateDto;
import com.wac.autocore.dto.invoice.InvoiceLineDto;
import com.wac.autocore.dto.workorder.WorkOrderSummaryDto;
import com.wac.autocore.exception.BusinessException;
import com.wac.autocore.gui.util.FormatUIUtil;
import com.wac.autocore.model.enums.WorkOrderState;
import com.wac.autocore.service.InvoiceService;
import com.wac.autocore.service.WorkOrderService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

    @FXML private TableView<InvoiceSummaryDto> invoiceTable;
    @FXML private TableColumn<InvoiceSummaryDto, Long> idColumn;
    @FXML private TableColumn<InvoiceSummaryDto, Long> workOrderIdColumn;
    @FXML private TableColumn<InvoiceSummaryDto, LocalDate> invoiceDateColumn;
    @FXML private TableColumn<InvoiceSummaryDto, BigDecimal> amountColumn;
    @FXML private TableColumn<InvoiceSummaryDto, BigDecimal> discountColumn;
    @FXML private TableColumn<InvoiceSummaryDto, BigDecimal> totalAmountColumn;
    @FXML private TableColumn<InvoiceSummaryDto, String> paidColumn;

    @FXML private TextField discountCodeField;
    @FXML private ComboBox<WorkOrderSummaryDto> workOrderComboBox;
    @FXML private Button detailsButton;

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

    public InvoiceController(InvoiceService invoiceService,
                             WorkOrderService workOrderService,
                             ApplicationContext applicationContext) {
        this.invoiceService = invoiceService;
        this.workOrderService = workOrderService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {

        if (invoiceTable != null) {

            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            workOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("workOrderId"));
            invoiceDateColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));

            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
            totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

            paidColumn.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(
                            getString(cellData.getValue().isPaid() ? "common.yes" : "common.no")
                    ));

            invoiceTable.setRowFactory(tv -> {
                TableRow<InvoiceSummaryDto> row = new TableRow<>();
                row.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && !row.isEmpty()) {
                        currentInvoiceId = row.getItem().getId();
                        navigateToInvoiceDetailsView();
                    }
                });
                return row;
            });

            detailsButton.disableProperty().bind(
                    invoiceTable.getSelectionModel().selectedItemProperty().isNull()
            );

            loadInvoiceData();
            invoiceTable.requestFocus();
        }

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
            ObservableList<InvoiceSummaryDto> invoiceData =
                    FXCollections.observableArrayList(invoiceService.getAllInvoices());
            invoiceTable.setItems(invoiceData);

        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));

        } catch (DataAccessException e) {
            logger.error("Could not load invoices", e);
            messages.showError(getString("invoice.error.load_invoices"));

        } catch (Exception e) {
            logger.error("Unexpected error", e);
            messages.showError(getString("invoice.error.load_invoices"));
        }
    }

    @FXML
    private void handleDetails() {
        InvoiceSummaryDto selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        currentInvoiceId = selected.getId();
        navigateToInvoiceDetailsView();
    }

    public void loadInvoiceDetails() {

        if (messages != null) {
            messages.clearMessage();
        }

        try {
            InvoiceDetailsDto dto = invoiceService.getInvoice(currentInvoiceId);

            idLabel.setText(String.valueOf(dto.getId()));
            workOrderLabel.setText(dto.getWorkOrderId() != null ? String.valueOf(dto.getWorkOrderId()) : "-");
            dateLabel.setText(FormatUIUtil.formatDate(dto.getInvoiceDate()));

            amountLabel.setText(dto.getAmount() != null ? dto.getAmount() + " SEK" : "-");
            totalLabel.setText(dto.getTotalAmount() != null ? dto.getTotalAmount() + " SEK" : "-");
            discountLabel.setText(dto.getDiscount() != null ? dto.getDiscount() + " SEK" : "-");

            paidLabel.setText(dto.isPaid() ? getString("common.yes") : getString("common.no"));

            lineNameOfServiceColumn.setCellValueFactory(new PropertyValueFactory<>("nameOfService"));
            lineAmountBeforeColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            lineDiscountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));

            lineTotalColumn.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));

            linesTable.getItems().setAll(dto.getLines());

        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));

        } catch (Exception e) {
            logger.error("Unexpected error", e);
            messages.showError(getString("invoice.error.load_details"));
        }
    }

    private void populateComboBox() {
        try {
            List<InvoiceSummaryDto> invoices = invoiceService.getAllInvoices();

            List<WorkOrderSummaryDto> workOrdersList = workOrderService.getAllWorkOrders().stream()
                    .filter(wo -> WorkOrderState.COMPLETED.equals(wo.getStatus()))
                    .filter(wo -> invoices.stream()
                            .noneMatch(inv -> inv.getWorkOrderId().equals(wo.getId())))
                    .collect(Collectors.toList());

            workOrderComboBox.setItems(FXCollections.observableArrayList(workOrdersList));

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

        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));

        } catch (Exception e) {
            logger.error("Unexpected error", e);
            messages.showError(getString("invoice.error.load_workorders"));
        }
    }

    @FXML
    private void handleNewInvoice() {
        if (messages != null) {
            messages.clearMessage();
        }
        loadCenterView("/com/wac/autocore/gui/view/NewInvoiceView.fxml");
    }

    @FXML
    private void handleSaveInvoice() {

        if (workOrderComboBox == null || workOrderComboBox.getValue() == null) {
            messages.showError(getString("invoice.error.select_workorder"));
            return;
        }

        Long workOrderId = workOrderComboBox.getValue().getId();
        String discountCode = discountCodeField != null ? discountCodeField.getText().trim() : "";

        InvoiceCreateDto dto = new InvoiceCreateDto(workOrderId, discountCode);

        try {
            invoiceService.createInvoice(dto);
            messages.showSuccess(getString("invoice.success.invoice_created"));
            navigateToInvoiceView();

        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));

        } catch (DataAccessException e) {
            logger.error("Could not create invoice", e);
            messages.showError(getString("invoice.error.create_invoice"));

        } catch (Exception e) {
            logger.error("Unexpected error", e);
            messages.showError(getString("invoice.error.create_invoice"));
        }
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


