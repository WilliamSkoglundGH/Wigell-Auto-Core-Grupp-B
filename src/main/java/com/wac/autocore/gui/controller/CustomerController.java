package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Customer;
import com.wac.autocore.service.CustomerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class CustomerController extends Controller{

    private final CustomerService customerService;
    private UserMessages messages;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Long> idColumn;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, Boolean> vipColumn;

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private CheckBox vipCheckBox;

    @FXML
    public void initialize() {
        if (customerTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
            vipColumn.setCellValueFactory(new PropertyValueFactory<>("vip"));

            loadCustomerData();
        }
    }

    public void loadCustomerData() {
        if (customerTable != null) {
            ObservableList<Customer> customerData =
                    FXCollections.observableArrayList(customerService.getAllCustomers());
            customerTable.setItems(customerData);
        }
    }

    @FXML
    private void handleNewCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/wac/autocore/gui/view/NewCustomerView.fxml"));

            loader.setController(this);
            Parent newCustomerView = loader.load();

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(newCustomerView);
                nameField.requestFocus();
            } else {
                messages.showError("Could not find BorderPane!");
            }

        } catch (IOException e) {
            e.printStackTrace();
            messages.showError("Could not load NewCustomerView.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveCustomer() {

        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        boolean isVip = vipCheckBox != null && vipCheckBox.isSelected();

        if (name == null || name.isEmpty()) {
            messages.showError("Please enter a name.");
            return;
        }

        if (email == null || email.isEmpty()) {
            messages.showError("Please enter an email.");
            return;
        }

        Customer newCustomer = customerService.createCustomer(name, phone, email, isVip);

        messages.showSuccess("Customer created: " + newCustomer.getName());
        navigateToCustomerView();
    }

    @FXML
    private void handleCancel() {
        navigateToCustomerView();
    }

    private void navigateToCustomerView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/wac/autocore/gui/view/CustomerView.fxml"));

            // Viktigt: skapa en NY controller med samma service
            CustomerController controller = new CustomerController(customerService);
            controller.setMessages(messages);

            loader.setController(controller);
            Parent customerView = loader.load();

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(customerView);
                controller.customerTable.requestFocus();
            } else {
                messages.showError("Could not find BorderPane!");
            }

        } catch (IOException e) {
            e.printStackTrace();
            messages.showError("Could not load CustomerView.fxml: " + e.getMessage());
        }
    }

    private BorderPane findMainLayout() {
        Scene scene = null;

        if (nameField != null && nameField.getScene() != null) {
            scene = nameField.getScene();
        } else if (customerTable != null && customerTable.getScene() != null) {
            scene = customerTable.getScene();
        }

        if (scene != null && scene.getRoot() != null) {
            Parent root = scene.getRoot();
            if (root instanceof BorderPane) {
                return (BorderPane) root;
            }
            return searchBorderPaneRecursive(root);
        }
        return null;
    }


    public void setMessages(UserMessages messages) {
        this.messages = messages;
    }
}


