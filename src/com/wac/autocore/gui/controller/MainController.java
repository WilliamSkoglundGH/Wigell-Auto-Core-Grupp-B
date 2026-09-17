package com.wac.autocore.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainRoot;

    @FXML
    private TextArea consoleOutput;

    // ---------------------------------------------------------
    // GENERIC VIEW LOADER
    // ---------------------------------------------------------
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainRoot.setCenter(view);

            log("Loaded view: " + fxmlPath);

        } catch (IOException e) {
            e.printStackTrace();
            log("ERROR loading: " + fxmlPath + " -> " + e.getMessage());
        }
    }

    private void log(String msg) {
        if (consoleOutput != null) {
            consoleOutput.appendText(msg + "\n");
        }
    }

    // ---------------------------------------------------------
    // MENU BUTTON ACTIONS
    // ---------------------------------------------------------

    @FXML
    private void showCustomerView() {
        loadView("/com/wac/autocore/gui/view/CustomerView.fxml");
    }

    @FXML
    private void showVehicleView() {
        loadView("/com/wac/autocore/gui/view/VehicleView.fxml");
    }

    @FXML
    private void showBookingView() {
        loadView("/com/wac/autocore/gui/view/BookingView.fxml");
    }

    @FXML
    private void showInvoiceView() {
        loadView("/com/wac/autocore/gui/view/InvoiceView.fxml");
    }

    @FXML
    private void showWorkOrderView() {
        loadView("/com/wac/autocore/gui/view/WorkOrderView.fxml");
    }

    @FXML
    private void showPaymentView() {
        loadView("/com/wac/autocore/gui/view/PaymentView.fxml");
    }

    @FXML
    private void showServiceItemView() {
        loadView("/com/wac/autocore/gui/view/ServiceItemView.fxml");
    }
}
