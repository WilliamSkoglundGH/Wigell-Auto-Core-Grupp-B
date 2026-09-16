package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.Invoice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

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
}