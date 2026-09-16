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
            } else {
                System.err.println("Kunde inte hitta BorderPane för att visa formuläret!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Kunde inte ladda NewCustomerView.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveCustomer() {
        if (nameField != null) {
            String name = nameField.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            boolean isVip = vipCheckBox != null && vipCheckBox.isSelected();

            if (name != null && !name.trim().isEmpty()) {
                int newId = Database.getCustomers().size() + 1;

                Customer newCustomer = new Customer(newId, name, phone, email);
                newCustomer.setVip(isVip);
                Database.getCustomers().add(newCustomer);

                navigateToCustomerView();
            }
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

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(customerView);
            } else {
                System.err.println("Kunde inte hitta BorderPane för att återgå till tabellen!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Kunde inte ladda tillbaka CustomerView.fxml: " + e.getMessage());
        }
    }

    // Extremt direkt och robust sökning efter BorderPane via scenens rot
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
            // Om roten i sin tur är en container, leta ett steg till
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
}