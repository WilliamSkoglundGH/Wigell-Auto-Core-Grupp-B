package com.wac.autocore.gui.controller;

import com.wac.autocore.model.Booking;
import com.wac.autocore.model.Mechanic;
import com.wac.autocore.service.MechanicService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class MechanicController extends OverController {

    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService, ApplicationContext applicationContext) {
        this.mechanicService = mechanicService;
        this.applicationContext = applicationContext;
    }

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
            ObservableList<Mechanic> mechanicData =
                    FXCollections.observableArrayList(mechanicService.getAllMechanics());
            mechanicTable.setItems(mechanicData);
        }
    }

    @FXML
    private void handleBookings() {

        if (messages != null) {
            messages.clearMessage();
        }

        Mechanic selected = mechanicTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            messages.showError("Please select a mechanic first.");
            return;
        }

        loadCenterView("/com/wac/autocore/gui/view/MechanicBookingsView.fxml");


        BorderPane mainLayout = findMainLayout();
        Parent centerView = mainLayout.getCenter().getParent();


        VBox bookingContainer = (VBox) centerView.lookup("#bookingContainer");


        List<Booking> bookings = mechanicService.getBookingsForMechanic(selected.getId());


        for (Booking booking : bookings) {

            Label label = new Label(
                    "Booking #" + booking.getId() +
                            " • " + booking.getDate() +
                            " • " + booking.getDescription()
            );


            bookingContainer.getChildren().add(label);
        }
    }

}





