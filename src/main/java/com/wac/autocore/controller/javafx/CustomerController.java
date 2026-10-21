package com.wac.autocore.controller.javafx;

import com.wac.autocore.dto.customer.CustomerCreateDto;
import com.wac.autocore.dto.customer.CustomerSummaryDto;
import com.wac.autocore.exception.BusinessException;
import com.wac.autocore.service.CustomerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
public class CustomerController extends OverController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService, ApplicationContext applicationContext) {
        this.customerService = customerService;
        this.applicationContext = applicationContext;
    }

    @FXML private TableView<CustomerSummaryDto> customerTable;
    @FXML private TableColumn<CustomerSummaryDto, Long> idColumn;
    @FXML private TableColumn<CustomerSummaryDto, String> nameColumn;
    @FXML private TableColumn<CustomerSummaryDto, String> emailColumn;
    @FXML private TableColumn<CustomerSummaryDto, String> phoneColumn;
    @FXML private TableColumn<CustomerSummaryDto, String> vipColumn;

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

            vipColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            getString(cellData.getValue().isVip()
                                    ? "common.yes"
                                    : "common.no")
                    )
            );

            loadCustomerData();
            customerTable.requestFocus();
        }

        if (nameField != null) {
            nameField.requestFocus();
        }

        if (phoneField != null) {
            phoneField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
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
            try {
                ObservableList<CustomerSummaryDto> customerData =
                        FXCollections.observableArrayList(customerService.getAllCustomers());
                customerTable.setItems(customerData);

            } catch (BusinessException e) {
                if (messages != null) {
                    messages.showError(getString(e.getMessage()));
                }

            } catch (DataAccessException e) {
                logger.error("Could not load customers from database. {}", e.getMessage(), e);
                if (messages != null) {
                    messages.showError(getString("customer.error.could_not_load"));
                }

            } catch (Exception e) {
                logger.error("Method: loadCustomerData | Could not load ", e);
                if (messages != null) {
                    messages.showError(getString("customer.error.could_not_load"));
                }
            }
        }
    }

    @FXML
    private void handleNewCustomer() {
        if (messages != null) {
            messages.clearMessage();
        }
        loadCenterView("/com/wac/autocore/controller/view/NewCustomerView.fxml");
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

        CustomerCreateDto dto = new CustomerCreateDto();
        dto.setName(name);
        dto.setPhone(phone);
        dto.setEmail(email);
        dto.setVip(isVip);

        try {
            CustomerSummaryDto newCustomer = customerService.createCustomer(dto);

            messages.showSuccess(
                    getString("customer_success.customer_created") + newCustomer.getName()
            );

            navigateToCustomerView();

        } catch (IllegalArgumentException e) {
            messages.showError(getString(e.getMessage()));

        } catch (Exception e) {
            logger.error("Could not save customer to database. {}", e.getMessage(), e);
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
        loadCenterView("/com/wac/autocore/controller/view/CustomerView.fxml");
    }
}

