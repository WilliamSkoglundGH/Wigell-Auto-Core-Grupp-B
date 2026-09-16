package com.wac.autocore.gui.controller;

import com.wac.autocore.gui.launcher.ConsoleRedirector;
import com.wac.autocore.gui.launcher.GarageServiceBridge;
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

    @FXML
    public void initialize() {
        // Omdirigera konsolen direkt vid start
        ConsoleRedirector.redirectTo(consoleOutput);
        System.out.println("Systemet startat med sidomeny-navigation.");
    }

    @FXML
    private void showCustomerView() {
        loadView("CustomerView.fxml");
    }

    @FXML
    private void showVehicleView() {
        loadView("VehicleView.fxml");
    }

    @FXML
    private void showBookingView() {
        loadView("BookingView.fxml");
    }
    @FXML
    private void showInvoiceView() {
        loadView("InvoiceView.fxml");
    }
    @FXML
    private void showWorkOrderView() {
        loadView("WorkOrderView.fxml");
    }
    @FXML
    private void showPaymentView() {
        loadView("PaymentView.fxml");
    }

    @FXML
    private void showServiceItemView() {
        loadView("ServiceItemView.fxml");
    }

    private void loadView(String fxmlFileName) {
        try {
            // Lägg till sökvägen från roten av dina resurser/paket
            String path = "/com/wac/autocore/gui/view/" + fxmlFileName;
            java.net.URL resourceUrl = getClass().getResource(path);

            if (resourceUrl == null) {
                System.err.println("Hittade inte filen: " + path);
                return;
            }

            Parent view = FXMLLoader.load(resourceUrl);
            mainRoot.setCenter(view);
        } catch (IOException e) {
            System.err.println("Kunde inte ladda vyn: " + fxmlFileName);
            e.printStackTrace();
        }
    }
}