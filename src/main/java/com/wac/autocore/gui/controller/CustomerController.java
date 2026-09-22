package com.wac.autocore.gui.controller;

import com.wac.autocore.data.Database;
import com.wac.autocore.model.Customer;
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

public class CustomerController {

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> idColumn;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, Boolean> vipColumn;

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private CheckBox vipCheckBox;

    private UserMessages messages;

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
            ObservableList<Customer> customerData = FXCollections.observableArrayList(
                    Database.getCustomers()
            );
            customerTable.setItems(customerData);
        }
    }

    @FXML
    private void handleNewCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/NewCustomerView.fxml"));
            loader.setController(this);
            Parent newCustomerView = loader.load();

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(newCustomerView);
                nameField.requestFocus();
            } else {
                messages.showError("Ccould not find BorderPane!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            messages.showError("Could not load NewCustomerView.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveCustomer() {
        if (nameField != null) {
            String name = nameField.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            boolean isVip = vipCheckBox != null && vipCheckBox.isSelected();

            String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

            if (name == null || name.trim().isEmpty()) {
                messages.showError("Please enter a name!");
                return;
            }

            if (email == null || !email.matches(emailRegex)) {
                messages.showError("Invalid email.");
                return;
            }

            int newId = Database.getCustomers().size() + 1;
            Customer newCustomer = new Customer(newId, name, phone, email);
            newCustomer.setVip(isVip);
            Database.getCustomers().add(newCustomer);
            messages.showSuccess("Customer created.");
            navigateToCustomerView();
        }
    }

    @FXML
    private void handleCancel() {
        navigateToCustomerView();
    }

    private void navigateToCustomerView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wac/autocore/gui/view/CustomerView.fxml"));
            Parent customerView = loader.load();
            CustomerController controller = loader.getController();
            controller.setMessages(messages);

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

    private BorderPane searchBorderPaneRecursive(Parent parent) {
        if (parent instanceof BorderPane) {
            return (BorderPane) parent;
        }
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                BorderPane found = searchBorderPaneRecursive((Parent) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    public void setMessages(UserMessages messages) {
        this.messages = messages;
    }
}