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
import org.springframework.dao.DataAccessException;

/**
 * Controller for the service item view.
 */
public class ServiceItemController extends Controller {

    @FXML
    private TableView<ServiceItem> serviceItemTable;

    @FXML
    private TableColumn<ServiceItem, Long> idColumn;

    @FXML
    private TableColumn<ServiceItem, String> nameColumn;

    @FXML
    private TableColumn<ServiceItem, String> descriptionColumn;

    @FXML
    private TableColumn<ServiceItem, Double> priceColumn;

    @FXML
    private TableColumn<ServiceItem, Integer> estimatedMinutesColumn;

    private final ServiceItemService serviceItemService;
    private UserMessages messages;
    private static final Logger logger = LoggerFactory.getLogger(ServiceItemController.class);

    public ServiceItemController(ServiceItemService serviceItemService){
        this.serviceItemService = serviceItemService;
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        estimatedMinutesColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedMinutes"));

        loadServiceItemData();
    }

    public void loadServiceItemData() {
        try {
            serviceItemTable.setItems(
                    FXCollections.observableArrayList(
                            serviceItemService.getAllServiceItems()
                    )
            );
        } catch (DataAccessException e) {
            logger.error("Could not load service items.", e);

            if (messages != null) {
                messages.showError("Could not load service items from database.");
            }
        }
    }

    public void setMessages(UserMessages messages) {
        this.messages = messages;
    }
}