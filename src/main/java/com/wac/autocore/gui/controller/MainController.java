package com.wac.autocore.gui.controller;

import com.sun.org.apache.xml.internal.security.utils.I18n;
import com.wac.autocore.gui.util.LanguageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import javafx.event.ActionEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Locale;

@Controller
public class MainController implements UserMessages {
    private final ApplicationContext applicationContext;

    public MainController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @FXML
    private BorderPane mainRoot;

    @FXML
    private Label messageLabel;

    @FXML
    private Button customerButton;

    private Button activeMenuButton;

    //Håller aktuell vy för uppdatering av språk.
    private String currentViewPath = "/com/wac/autocore/gui/view/CustomerView.fxml";

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), LanguageManager.getBundle());
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();

            Object controller = loader.getController();
//mönster? vi har en OverController, vilket mönster?? hur??????????????????????????????????????????????????????????????????????????
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
            currentViewPath = fxmlPath;

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

    // ---------------------------------------------------------
    // LANGUAGE
    // ------------------------------------------------------

    @FXML
    private void handleToggleLanguage() {
        Locale current = LanguageManager.getBundle().getLocale();
        Locale next = current.getLanguage().equals("sv")
                ? new Locale("en")
                : new Locale("sv");

        LanguageManager.setLocale(next);      // uppdaterar bundel i util-klassen.
        loadView(currentViewPath, activeMenuButton); // Laddar om vyn som borde nu har nya språket.
    }
}
