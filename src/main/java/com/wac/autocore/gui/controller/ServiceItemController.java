package com.wac.autocore.gui.controller;

import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.service.ServiceItemService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;

@Controller
public class ServiceItemController extends OverController {

    @FXML private TableView<ServiceItem> serviceItemTable;
    @FXML private TableColumn<ServiceItem, Long> idColumn;
    @FXML private TableColumn<ServiceItem, String> nameColumn;
    @FXML private TableColumn<ServiceItem, String> descriptionColumn;
    @FXML private TableColumn<ServiceItem, Double> priceColumn;
    @FXML private TableColumn<ServiceItem, Integer> estimatedMinutesColumn;

    private final ServiceItemService serviceItemService;
    private static final Logger logger = LoggerFactory.getLogger(ServiceItemController.class);

    // Spring injicerar ServiceItemService och ApplicationContext (som skickas vidare till OverController)
    public ServiceItemController(ServiceItemService serviceItemService, ApplicationContext applicationContext) {
        this.serviceItemService = serviceItemService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        if (serviceItemTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
            estimatedMinutesColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedMinutes"));

            loadServiceItemData();
            serviceItemTable.requestFocus();
        }
    }

    public void loadServiceItemData() {
        if (serviceItemTable != null) {
            try {
                serviceItemTable.setItems(
                        FXCollections.observableArrayList(
                                serviceItemService.getAllServiceItems()
                        )
                );
            } catch (DataAccessException e) {
                logger.error("Could not load service items.", e);

                if (messages != null) {
                    messages.showError(getString("serviceitem.error.could_not_load"));
                }
            }
        }
    }
}