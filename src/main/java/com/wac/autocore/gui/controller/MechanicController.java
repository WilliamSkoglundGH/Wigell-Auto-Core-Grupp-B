package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Mechanic;
import com.wac.autocore.service.MechanicService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
public class MechanicController extends OverController {

    @FXML private TableView<Mechanic> mechanicTable;
    @FXML private TableColumn<Mechanic, Long> idColumn;
    @FXML private TableColumn<Mechanic, String> nameColumn;
    @FXML private TableColumn<Mechanic, String> phoneColumn;
    @FXML private TableColumn<Mechanic, String> specializationColumn;

    private static final Logger logger = LoggerFactory.getLogger(MechanicController.class);
    private final MechanicService mechanicService;

    // Spring injicerar MechanicService och ApplicationContext
    public MechanicController(MechanicService mechanicService, ApplicationContext applicationContext) {
        this.mechanicService = mechanicService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        if (mechanicTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
            specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));

            loadMechanicData();
            mechanicTable.requestFocus();
        }
    }

    public void loadMechanicData() {
        if (mechanicTable != null) {
            try {
                ObservableList<Mechanic> mechanicData = FXCollections.observableArrayList(
                        mechanicService.getAllMechanics()
                );
                mechanicTable.setItems(mechanicData);
            } catch (Exception e) {
                logger.error("Could not load mechanics from database. {}", e.getMessage(), e);
                if (messages != null) {
                    messages.showError("Could not load mechanics from database.");
                }
            }
        }
    }
}