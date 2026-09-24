package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Customer;
import com.wac.autocore.service.CustomerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
public class CustomerController extends OverController {

    private final CustomerService customerService;

    // Spring injicerar både CustomerService och ApplicationContext automatiskt
    public CustomerController(CustomerService customerService, ApplicationContext applicationContext) {
        this.customerService = customerService;
        this.applicationContext = applicationContext;
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
            customerTable.requestFocus();
        }

        if (nameField != null) {
            nameField.requestFocus();
        }

        if (phoneField != null) {
            phoneField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    // Tar bort allt som inte är siffror
                    String digits = newValue.replaceAll("\\D", "");

                    if (digits.length() > 3) {
                        phoneField.setText(digits.substring(0, 3) + "-" + digits.substring(3));
                    } else {
                        phoneField.setText(digits);
                    }
                }
            });
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
        if (messages != null) {
            messages.clearMessage();
        }
        // Använder den gemensamma metoden – språket och Spring-kontexten följer med automatiskt!
        loadCenterView("/com/wac/autocore/gui/view/NewCustomerView.fxml");
    }

    @FXML
    private void handleSaveCustomer() {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        boolean isVip = vipCheckBox != null && vipCheckBox.isSelected();

        if (name == null || name.trim().isEmpty()) {
            messages.showError(getString("customer.error.enter_name"));
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            messages.showError(getString("customer.error.enter_email"));
            return;
        }

        try {
            Customer newCustomer = customerService.createCustomer(name, phone, email, isVip);
            messages.showSuccess(getString("customer_success.customer_created") + newCustomer.getName());
            navigateToCustomerView();
        } catch (Exception e) {
            messages.showError(getString("customer.error.create_customer"));
        }
    }

    @FXML
    private void handleCancel() {
        if (messages != null) {
            messages.clearMessage();
        }
        navigateToCustomerView();
    }

    private void navigateToCustomerView() {
        loadCenterView("/com/wac/autocore/gui/view/CustomerView.fxml");
    }

    // findMainLayout() är borttagen härifrån eftersom den nu ärvs från OverController!
}