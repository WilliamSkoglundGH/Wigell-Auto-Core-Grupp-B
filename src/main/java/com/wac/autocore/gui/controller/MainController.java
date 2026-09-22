package com.wac.autocore.gui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import javafx.event.ActionEvent;
import java.io.IOException;

public class MainController implements UserMessages {

    @FXML
    private BorderPane mainRoot;

    @FXML
    private Label messageLabel;

    @FXML
    private Button customerButton;

    private Button activeMenuButton;

    @FXML
    public void initialize() {
        loadView("/com/wac/autocore/gui/view/CustomerView.fxml", customerButton);
    }

    private void markActiveMenuButton(Button button) {
        if (activeMenuButton != null) {
            activeMenuButton.getStyleClass().remove("active-button");
        }

        activeMenuButton = button;
        activeMenuButton.getStyleClass().add("active-button");
    }



    // ---------------------------------------------------------
    // GENERIC VIEW LOADER
    // ---------------------------------------------------------
    private void loadView(String fxmlPath, Button menuButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();

            if (controller instanceof BookingController) {
                BookingController bookingController = (BookingController) controller;
                bookingController.setMessages(this);
            } else if (controller instanceof CustomerController) {
                CustomerController customerController = (CustomerController) controller;
                customerController.setMessages(this);

            } else if (controller instanceof InvoiceController) {
                InvoiceController invoiceController = (InvoiceController) controller;
                invoiceController.setMessages(this);

            } else if (controller instanceof PaymentController) {
                PaymentController paymentController = (PaymentController) controller;
                paymentController.setMessages(this);

            } else if (controller instanceof ServiceItemController) {
                ServiceItemController serviceItemController = (ServiceItemController) controller;
                serviceItemController.setMessages(this);
            } else if (controller instanceof VehicleController) {
                VehicleController vehicleController = (VehicleController) controller;
                vehicleController.setMessages(this);
            } else if (controller instanceof WorkOrderController) {
                WorkOrderController workOrderController = (WorkOrderController) controller;
                workOrderController.setMessages(this);
            }

            mainRoot.setCenter(view);
            markActiveMenuButton(menuButton);

            clearMessage();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not open the requested view. Please try again.");
        }
    }

    @Override
    public void showSuccess(String message) {
        messageLabel.getStyleClass().removeAll("msg-error", "msg-success");
        messageLabel.getStyleClass().add("msg-success");
        messageLabel.setText(message);
    }

    @Override
    public void showError(String message) {
        messageLabel.getStyleClass().removeAll("msg-error", "msg-success");
        messageLabel.getStyleClass().add("msg-error");
        messageLabel.setText(message);
    }

    @Override
    public void clearMessage() {
        messageLabel.getStyleClass().removeAll("msg-error", "msg-success");
        messageLabel.setText("");
    }


    // ---------------------------------------------------------
    // MENU BUTTON ACTIONS
    // ---------------------------------------------------------

    @FXML
    private void showCustomerView(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        loadView("/com/wac/autocore/gui/view/CustomerView.fxml", clickedButton);
    }

    @FXML
    private void showVehicleView(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        loadView("/com/wac/autocore/gui/view/VehicleView.fxml", clickedButton);
    }

    @FXML
    private void showBookingView(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        loadView("/com/wac/autocore/gui/view/BookingView.fxml", clickedButton);
    }

    @FXML
    private void showInvoiceView(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        loadView("/com/wac/autocore/gui/view/InvoiceView.fxml", clickedButton);
    }

    @FXML
    private void showWorkOrderView(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        loadView("/com/wac/autocore/gui/view/WorkOrderView.fxml", clickedButton);
    }

    @FXML
    private void showPaymentView(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        loadView("/com/wac/autocore/gui/view/PaymentView.fxml", clickedButton);
    }

    @FXML
    private void showServiceItemView(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        loadView("/com/wac/autocore/gui/view/ServiceItemView.fxml", clickedButton);
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}
