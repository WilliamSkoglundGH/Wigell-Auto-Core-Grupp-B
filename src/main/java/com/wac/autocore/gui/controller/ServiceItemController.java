package com.wac.autocore.gui.controller;

import com.wac.autocore.dto.ServicePackageSummaryDto;
import com.wac.autocore.gui.util.BigDecimalStringConverter;
import com.wac.autocore.model.ServiceItem;
import com.wac.autocore.model.ServicePackage;
import com.wac.autocore.service.ServiceItemService;
import com.wac.autocore.service.ServicePackageService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Scope("prototype")
public class ServiceItemController extends OverController {

    @FXML
    private TableView<ServiceItem> serviceItemTable;
    @FXML
    private TableColumn<ServiceItem, Long> idColumn;
    @FXML
    private TableColumn<ServiceItem, String> nameColumn;
    @FXML
    private TableColumn<ServiceItem, String> descriptionColumn;
    @FXML
    private TableColumn<ServiceItem, BigDecimal> priceColumn;
    @FXML
    private TableColumn<ServiceItem, Integer> estimatedMinutesColumn;

    @FXML
    private TableView<ServicePackageSummaryDto> servicePackageTable;
    @FXML
    private TableColumn<ServicePackageSummaryDto, Long> packageIdColumn;
    @FXML
    private TableColumn<ServicePackageSummaryDto, String> packageNameColumn;
    @FXML
    private TableColumn<ServicePackageSummaryDto, String> packageDescriptionColumn;
    @FXML
    private TableColumn<ServicePackageSummaryDto, String> packageServicesColumn;
    @FXML
    private TableColumn<ServicePackageSummaryDto, String> packageActiveColumn;


    @FXML
    private ListView<ServiceItem> servicesListView;
    @FXML
    private TextArea packageDescriptionArea;
    @FXML
    private TextField packageNameField;

    @FXML
    final Map<Long, BooleanProperty> serviceSelections = new HashMap<>();

    private final ServiceItemService serviceItemService;
    private final ServicePackageService servicePackageService;
    private static final Logger logger = LoggerFactory.getLogger(ServiceItemController.class);

    public ServiceItemController(ServiceItemService serviceItemService, ServicePackageService servicePackageService, ApplicationContext applicationContext) {
        this.serviceItemService = serviceItemService;
        this.servicePackageService = servicePackageService;
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
        if (packageNameField != null) {
            serviceSelections.clear();
            packageNameField.clear();
            if (packageDescriptionArea != null) {
                packageDescriptionArea.clear();
            }
            loadServiceList();
        }
        if (servicePackageTable != null) {
            packageIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            packageNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            packageDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            packageServicesColumn.setCellValueFactory(new PropertyValueFactory<>("services"));

            // Använd getString för att få rätt språk i tabellen
            packageActiveColumn.setCellValueFactory(cellData -> {
                boolean active = cellData.getValue().isActive();
                String text = active ? getString("common.yes") : getString("common.no");
                return new javafx.beans.property.SimpleStringProperty(text);
            });

            // Ladda in datan i tabellen!
            loadServicePackageData();
        }
    }
    private void loadServicePackageData() {
        if (servicePackageTable != null) {
            try {
                servicePackageTable.setItems(
                        FXCollections.observableArrayList(
                                servicePackageService.getAllPackages()
                        )
                );
            } catch (Exception e) {
                logger.error("Could not load service packages.", e);
                if (messages != null) {
                    messages.showError(getString("serviceitem.error.could_not_load"));
                }
            }
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

    private void loadServiceList() {
        if (servicesListView != null) {
            ObservableList<ServiceItem> serviceItems =
                    FXCollections.observableArrayList(serviceItemService.getAllServiceItems());

            for (ServiceItem item : serviceItems) {
                serviceSelections.putIfAbsent(item.getId(),
                        new SimpleBooleanProperty(false));
            }

            servicesListView.setCellFactory(CheckBoxListCell.forListView(
                    item -> serviceSelections.get(item.getId()),
                    new StringConverter<ServiceItem>() {
                        @Override
                        public String toString(ServiceItem item) {

                            String text = item.getName() + " — " + item.getPrice() + " SEK"
                                    + " — " + item.getEstimatedMinutes() + " min";

                            String description = item.getDescription();

                            if (description != null && !description.trim().isEmpty()) {
                                text += "\n" + description;
                            }
                            return text;
                        }

                        @Override
                        public ServiceItem fromString(String text) {
                            return null;
                        }
                    }
            ));

            servicesListView.setItems(serviceItems);
        }
    }

    public void handleSavePackage() {

        try {
            String packageName = packageNameField.getText();
            if (packageName == null || packageName.trim().length() == 0) {
                messages.showError(getString("serviceitem.error.package.invalid_name"));
                return;
            }
            String packageDescription = packageDescriptionArea.getText();


            List<ServiceItem> selectedServices = servicesListView.getItems().stream()
                    .filter(item -> serviceSelections.containsKey(item.getId()) && serviceSelections.get(item.getId()).get())
                    .collect(Collectors.toList());

            if (selectedServices.isEmpty()) {
                messages.showError(getString("booking.error.select_services"));
                return;
            }
            servicePackageService.savePackage(packageName, packageDescription, selectedServices);
            messages.showSuccess(getString("serviceItem.success.package_created"));
            navigateToServiceItemView();

        } catch (Exception e) {

        }
    }

    @FXML
    public void handleNewServiceItemOrder() {

    }

    @FXML
    public void handleShowPackages() {


        if (messages != null) {
            messages.clearMessage();
        }
        loadCenterView("/com/wac/autocore/gui/view/ServicePackageView.fxml");
    }

    public void handleNewPackage() {

        if (messages != null) {
            messages.clearMessage();
        }
        loadCenterView("/com/wac/autocore/gui/view/NewPackageView.fxml");
    }

    @FXML
    private void handleCancel() {
        if (messages != null) {
            messages.clearMessage();
        }
        navigateToServiceItemView();
    }

    private void navigateToServiceItemView() {
        loadCenterView("/com/wac/autocore/gui/view/ServiceItemView.fxml");
    }
}
