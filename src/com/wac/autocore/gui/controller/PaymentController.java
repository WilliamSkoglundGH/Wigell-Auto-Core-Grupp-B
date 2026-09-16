package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.Payment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;

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
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        paymentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        successfulColumn.setCellValueFactory(new PropertyValueFactory<>("successful"));

        loadPaymentData();
    }

    public void loadPaymentData() {
        ObservableList<Payment> paymentData = FXCollections.observableArrayList(
                Database.getPayments()
        );

        paymentTable.setItems(paymentData);
    }
}