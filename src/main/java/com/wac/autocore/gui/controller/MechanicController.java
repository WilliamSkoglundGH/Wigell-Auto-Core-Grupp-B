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

public class MechanicController extends Controller{
    @FXML
    private TableView<Mechanic> mechanicTable;
    @FXML
    private TableColumn<Mechanic, Long> idColumn;
    @FXML
    private TableColumn<Mechanic, String> nameColumn;
    @FXML
    private TableColumn<Mechanic, String> phoneColumn;
    @FXML
    private TableColumn<Mechanic, String> specializationColumn;

    private static final Logger logger = LoggerFactory.getLogger(MechanicController.class);


    private UserMessages messages;
    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }
    @FXML
    public void initialize() {
        if (mechanicTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
            specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));

            loadMechanicData();
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

    public void setMessages(UserMessages messages) {
        this.messages = messages;
    }
}
