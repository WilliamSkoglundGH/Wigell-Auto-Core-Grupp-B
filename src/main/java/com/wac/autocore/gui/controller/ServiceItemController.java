package com.wac.autocore.gui.controller;

import com.wac.autocore.gui.util.BigDecimalStringConverter;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.service.ServiceItemService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
@Scope("prototype")
public class ServiceItemController extends OverController {

    @FXML private TableView<ServiceItem> serviceItemTable;
    @FXML private TableColumn<ServiceItem, Long> idColumn;
    @FXML private TableColumn<ServiceItem, String> nameColumn;
    @FXML private TableColumn<ServiceItem, String> descriptionColumn;
    @FXML private TableColumn<ServiceItem, BigDecimal> priceColumn;
    @FXML private TableColumn<ServiceItem, Integer> estimatedMinutesColumn;

    private final ServiceItemService serviceItemService;
    private static final Logger logger = LoggerFactory.getLogger(ServiceItemController.class);

    public ServiceItemController(ServiceItemService serviceItemService, ApplicationContext applicationContext) {
        this.serviceItemService = serviceItemService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        if (serviceItemTable != null) {

            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(cellData -> {
                ServiceItem item = cellData.getValue();
                String key = "serviceitem." + item.getCode() + ".name";
                return new SimpleStringProperty(getString(key));
            });

            descriptionColumn.setCellValueFactory(cellData -> {
                ServiceItem item = cellData.getValue();
                String key = "serviceitem." + item.getCode() + ".description";
                return new SimpleStringProperty(getString(key));
            });
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
            estimatedMinutesColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedMinutes"));

            serviceItemTable.setEditable(true);

            priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));

            priceColumn.setOnEditCommit(event -> {
                ServiceItem item = event.getRowValue();
                BigDecimal newPrice = event.getNewValue();

                int rowIndex = event.getTablePosition().getRow();

                try {
                    serviceItemService.changePrice(item.getId(), newPrice);
                    item.setPrice(newPrice);

                    logger.info("Pris ändrat för serviceItem {} till {}", item.getId(), newPrice);

                    messages.showSuccess(
                            getString("serviceitem.price_changed") + " " + newPrice
                    );

                } catch (Exception e) {
                    logger.error("Kunde inte ändra priset för serviceItem {}: {}", item.getId(), e.getMessage(), e);

                    messages.showError(
                            getString("serviceitem.price_change_failed") + " " + e.getMessage()
                    );
                }



                // allt jag vill är att stå kvar på samma raaaaaad!!
                javafx.application.Platform.runLater(() -> {
                    javafx.application.Platform.runLater(() -> {
                        serviceItemTable.getSelectionModel().select(rowIndex);
                        serviceItemTable.scrollTo(rowIndex);
                        serviceItemTable.requestFocus();
                    });
                });
            });



            loadServiceItemData();
            serviceItemTable.requestFocus();
        }
    }

    public void loadServiceItemData() {
        if (serviceItemTable != null) {
            try {
                java.util.List<ServiceItem> items = serviceItemService.getAllServiceItems();

                for (ServiceItem item : items) {
                    String name = item.getName();
                    if (name != null) {
                        // gör om "Oil change" → "oilchange"
                        String code = name.toLowerCase().replace(" ", "");
                        item.setCode(code);
                    }
                }

                serviceItemTable.setItems(
                        FXCollections.observableArrayList(items)
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
