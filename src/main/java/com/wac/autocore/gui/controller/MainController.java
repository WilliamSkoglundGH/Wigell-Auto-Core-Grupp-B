package com.wac.autocore.gui.controller;

import com.wac.autocore.gui.util.LanguageManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

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

    // Menyknappar med fx:id för direkt uppdatering av språk
    @FXML private Button customerButton;
    @FXML private Button vehicleButton;
    @FXML private Button bookingButton;
    @FXML private Button workOrderButton;
    @FXML private Button invoiceButton;
    @FXML private Button paymentButton;
    @FXML private Button serviceItemButton;
    @FXML private Button mechanicButton;
    @FXML private Button languageButton;
    @FXML private Button exitButton;

    private Button activeMenuButton;

    // Håller koll på aktuell vy så att vi stannar kvar på rätt skärm vid språkbyte
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
        if (activeMenuButton != null) {
            activeMenuButton.getStyleClass().add("active-button");
        }
    }

    // ---------------------------------------------------------
    // GENERIC VIEW LOADER
    // ---------------------------------------------------------
    private void loadView(String fxmlPath, Button menuButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), LanguageManager.getBundle());
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();

            // Skicka med både meddelanden och ApplicationContext till OverController-subklasser
            Object controller = loader.getController();
            if (controller instanceof OverController) {
                OverController oc = (OverController) controller;
                oc.setMessages(this);
                oc.setApplicationContext(applicationContext);
            }



            mainRoot.setCenter(view);
            markActiveMenuButton(menuButton);

            clearMessage();
            currentViewPath = fxmlPath; // Sparar aktiv vy

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
    private void showMechanicView(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        loadView("/com/wac/autocore/gui/view/MechanicView.fxml", clickedButton);
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    // ---------------------------------------------------------
    // LANGUAGE
    // ---------------------------------------------------------

    @FXML
    private void handleToggleLanguage() {
        Locale current = LanguageManager.getBundle().getLocale();
        Locale next = current.getLanguage().equals("sv")
                ? new Locale("en")
                : new Locale("sv");

        LanguageManager.setLocale(next); // Uppdaterar bundle i util-klassen

        // 1. Uppdatera texterna på menyknapparna direkt
        updateMenuButtonTexts();

        // 2. Ladda om den aktiva centervyn med det nya språket
        loadView(currentViewPath, activeMenuButton);
    }

    private void updateMenuButtonTexts() {
        ResourceBundle bundle = LanguageManager.getBundle();
        if (customerButton != null) customerButton.setText(bundle.getString("nav.customers"));
        if (vehicleButton != null) vehicleButton.setText(bundle.getString("nav.vehicles"));
        if (bookingButton != null) bookingButton.setText(bundle.getString("nav.bookings"));
        if (workOrderButton != null) workOrderButton.setText(bundle.getString("nav.workorders"));
        if (invoiceButton != null) invoiceButton.setText(bundle.getString("nav.invoices"));
        if (paymentButton != null) paymentButton.setText(bundle.getString("nav.payments"));
        if (serviceItemButton != null) serviceItemButton.setText(bundle.getString("nav.serviceitems"));
        if (mechanicButton != null) mechanicButton.setText(bundle.getString("nav.mechanics"));
        if (languageButton != null) languageButton.setText(bundle.getString("nav.language"));
        if (exitButton != null) exitButton.setText(bundle.getString("nav.exit"));
    }
    // Låt andra controllers tala om vilken vy som för närvarande är aktiv
    public void setCurrentViewPath(String viewPath) {
        this.currentViewPath = viewPath;
    }
}