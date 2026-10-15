package com.wac.autocore.controller.javafx;

import com.wac.autocore.dto.booking.BookingFullDto;
import com.wac.autocore.dto.mechanic.MechanicSummaryDto;
import com.wac.autocore.dto.serviceItem.ServiceItemSummaryDto;
import com.wac.autocore.dto.vechicle.VehicleSummaryDto;
import com.wac.autocore.dto.workorder.WorkOrderCreateDto;
import com.wac.autocore.dto.workorder.WorkOrderDetailsDto;
import com.wac.autocore.dto.workorder.WorkOrderServiceItemDto;
import com.wac.autocore.dto.workorder.WorkOrderSummaryDto;
import com.wac.autocore.exception.*;
import com.wac.autocore.controller.util.FormatUIUtil;
import com.wac.autocore.model.enums.BookingState;
import com.wac.autocore.model.enums.WorkOrderState;
import com.wac.autocore.model.enums.WorkOrderType;
import com.wac.autocore.service.BookingService;
import com.wac.autocore.service.MechanicService;
import com.wac.autocore.service.ServiceItemService;
import com.wac.autocore.service.VehicleService;
import com.wac.autocore.service.WorkOrderService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@Scope("prototype")
public class WorkOrderController extends OverController {

    // WORKORDER-MANAGEMENT TABLE (WorkOrderView.fxml)
    @FXML
    private TableView<WorkOrderSummaryDto> workOrderTable;
    @FXML
    private TableColumn<WorkOrderSummaryDto, Long> idColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> bookingIdColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> mechanicIdColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> statusColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> startTimeColumn;
    @FXML
    private TableColumn<WorkOrderSummaryDto, String> endTimeColumn;
    @FXML
    private TableColumn<WorkOrderDetailsDto, WorkOrderType> claimsColumn;
    @FXML
    private Button detailsButton;


    // WORK ORDER-DETAILS (WorkOrderInfoView.fxml)
    @FXML
    private Label statusLabel;
    @FXML
    private Label mechanicLabel;
    @FXML
    private Label vehicleLabel;
    @FXML
    private Label customerLabel;
    @FXML
    private Label startTimeLabel;
    @FXML
    private Label endTimeLabel;
    @FXML
    private Label estTimeLabel;
    @FXML
    private Label estPriceLabel;
    @FXML
    private Label bookingIdLabel;
    @FXML
    private TableView<WorkOrderServiceItemDto> serviceItemTable;
    @FXML
    private TableColumn<WorkOrderServiceItemDto, String> serviceNameColumn;
    @FXML
    private TableColumn<WorkOrderServiceItemDto, BigDecimal> servicePriceColumn;
    @FXML
    private TableColumn<WorkOrderServiceItemDto, Integer> serviceDurationColumn;
    @FXML
    private Button startButton;
    @FXML
    private Button completeButton;
    @FXML
    private Button claimsButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelWorkOrderButton;
    @FXML
    private Label workOrderClaimLabel;


    // UPDATE DRAFT (UpdateWorkOrderDraftView.fxml)
    @FXML
    private ComboBox<String> vehicleComboUpdate;
    @FXML
    private DatePicker datePickerUpdate;
    @FXML
    private ListView<ServiceItemSummaryDto> servicesListViewUpdate;
    @FXML
    private TextArea descriptionFieldUpdate;
    @FXML
    private ComboBox<MechanicSummaryDto> mechanicFieldUpdate;
    @FXML
    private Button confirmDraftButton;
    @FXML
    private Button updateDraftButton;

    // CLAIMS DRAFT (NewClaimsViewv.fxml)
    @FXML
    private TextArea descriptionFieldClaim;

    // NEW DROP-IN
    @FXML
    private ComboBox<VehicleSummaryDto> vehicleComboDropIn;
    @FXML
    private DatePicker datePickerDropIn;
    @FXML
    private ListView<ServiceItemSummaryDto> servicesListViewDropIn;
    @FXML
    private TextArea descriptionFieldDropIn;
    @FXML
    private ComboBox<MechanicSummaryDto> mechanicFieldDropIn;

    // NEW WORK ORDER (NewWorkOrderView.fxml)
    @FXML
    private ComboBox<BookingFullDto> bookingComboBox;
    //   @FXML
    //   private ListView<ServiceItem> servicesListView;

    private final Map<Long, javafx.beans.property.BooleanProperty> serviceSelections = new HashMap<>();
    private final Map<Long, Long> draftServiceRowIds = new HashMap<>();

    private static Long currentWorkOrderId;

    private final WorkOrderService workOrderService;
    private final ServiceItemService serviceItemService;
    private final BookingService bookingService;
    private final MechanicService mechanicService;
    private final VehicleService vehicleService;

    private static final Logger logger = LoggerFactory.getLogger(WorkOrderController.class);

    public WorkOrderController(BookingService bookingService, ServiceItemService serviceItemService,
                               WorkOrderService workOrderService, ApplicationContext applicationContext,
                               MechanicService mechanicService, VehicleService vehicleService) {
        this.bookingService = bookingService;
        this.serviceItemService = serviceItemService;
        this.workOrderService = workOrderService;
        this.applicationContext = applicationContext;
        this.mechanicService = mechanicService;
        this.vehicleService = vehicleService;
    }

    // ---------------------------------------------------------
    // INITIALIZE
    // ---------------------------------------------------------
    @FXML
    public void initialize() {
        // WorkOrderView.fxml
        if (workOrderTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            bookingIdColumn.setCellValueFactory(cellData -> {
                Long bookingId = cellData.getValue().getBookingId();
                return new SimpleStringProperty(bookingId != null ? bookingId.toString() : "-");
            });

            mechanicIdColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMechanicName()));

            statusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            getString("status." + cellData.getValue().getStatus())
                    )
            );

            startTimeColumn.setCellValueFactory(cellData -> {
                LocalDateTime startTime = cellData.getValue().getStartTime();
                String formattedTime = (startTime != null) ? FormatUIUtil.formatTime(startTime) : "";
                return new javafx.beans.property.SimpleStringProperty(formattedTime);
            });

            endTimeColumn.setCellValueFactory(cellData -> {
                LocalDateTime endTime = cellData.getValue().getEndTime();
                String formattedTime = (endTime != null) ? FormatUIUtil.formatTime(endTime) : "";
                return new javafx.beans.property.SimpleStringProperty(formattedTime);
            });
            if (detailsButton != null) {
                detailsButton.disableProperty().bind(
                        workOrderTable.getSelectionModel().selectedItemProperty().isNull()
                );
            }

            loadWorkOrderData();
            workOrderTable.requestFocus();
            workOrderTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    WorkOrderSummaryDto selectedOrder = workOrderTable.getSelectionModel().getSelectedItem();
                    if (selectedOrder != null) {
                        currentWorkOrderId = selectedOrder.getId();
                        navigateToWorkOrderInfoView();
                    }
                }
            });
        }

        loadBookingComboBox();
        if (statusLabel != null && currentWorkOrderId != null) {
            loadWorkOrderDetails();
        }
        if (mechanicFieldUpdate != null && currentWorkOrderId != null) {
            loadDraftForm();
        }
        if (vehicleComboDropIn != null) {
            loadDropInForm();
        }
    }


    public void loadWorkOrderData() {
        if (workOrderTable != null) {
            try {
                ObservableList<WorkOrderSummaryDto> workOrderData = FXCollections.observableArrayList(
                        workOrderService.getAllWorkOrders()
                );
                workOrderTable.setItems(workOrderData);
            } catch (Exception e) {
                logger.error("Could not load work orders from database. {}", e.getMessage(), e);
            }
        }
    }

    @FXML
    private void handleStartWorkOrder() {

        try {
            workOrderService.startWorkOrder(currentWorkOrderId);
            loadWorkOrderDetails();
            messages.showSuccess(getString("workorder.success.workorder_started"));
        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));
        } catch (Exception e) {
            logger.error("Could not save to database. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    @FXML
    private void handleConfirmWorkOrder() {
        try {
            workOrderService.confirmWorkOrder(currentWorkOrderId);
            loadWorkOrderDetails();
            messages.showSuccess(getString("workorder.success.workorder_confirmed"));

        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));

        } catch (Exception e) {
            logger.error("Could not confirm work order.", e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    @FXML
    private void handleCancelWorkOrder() {
        try {
            workOrderService.cancelWorkOrder(currentWorkOrderId);
            loadWorkOrderDetails();
            messages.showSuccess(getString("workorder.success.workorder_canceled"));

        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));

        } catch (Exception e) {
            logger.error("Could not cancel work order.", e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    @FXML
    private void handleCompleteWorkOrder() {

        try {
            workOrderService.completeWorkOrder(currentWorkOrderId);
            loadWorkOrderDetails();
            messages.showSuccess(getString("workorder.success.workorder_completed"));
            loadWorkOrderData();

        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));

        } catch (Exception e) {
            logger.error("Could not complete work order.", e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    @FXML
    private void handleNewWorkOrder() {
        if (messages != null) {
            messages.clearMessage();
        }
        loadCenterView("/com/wac/autocore/controller/view/NewWorkOrderView.fxml");
    }

    @FXML
    private void handleDetails() {
        Long id = workOrderTable.getSelectionModel().getSelectedItem().getId();
        if (id == null) {
            return;
        }
        currentWorkOrderId = id;
        navigateToWorkOrderInfoView();
    }

    private void loadBookingComboBox() {
        if (bookingComboBox != null) {
            try {
                List<BookingFullDto> bookedList =
                        bookingService.getAllBookingsWithServiceItems().stream()
                                .filter(b -> BookingState.BOOKED.equals(b.getStatus()))
                                .collect(Collectors.toList());
//
                bookingComboBox.setItems(FXCollections.observableArrayList(bookedList));

                bookingComboBox.setConverter(new StringConverter<BookingFullDto>() {
                    @Override
                    public String toString(BookingFullDto booking) {
                        if (booking == null) {
                            return "";
                        }

                        String description = booking.getDescription();
                        return getString("booking.option.id") + ": " + booking.getId()
                                + " | " + getString("booking.option.vehicle") + ": "
                                + booking.getVehicle().getRegistrationNumber()
                                + " | " + getString("booking.option.date") + ": "
                                + booking.getDate()
                                + " | " + getString("booking.option.mechanic") + ": "
                                + booking.getMechanic().getName()
                                + (description == null || description.trim().isEmpty()
                                ? ""
                                : " | " + getString("booking.option.description")
                                + ": " + description);
                    }

                    @Override
                    public BookingFullDto fromString(String s) {
                        return null;
                    }

                });
            } catch (NullPointerException e) {
                messages.showError(getString("workorder.error.no_bookings"));
            }
            bookingComboBox.requestFocus();
        }
    }

    @FXML
    private void handleSaveWorkOrder() {
        try {
            if (bookingComboBox != null) {
                BookingFullDto selectedBooking = bookingComboBox.getValue();
                if (selectedBooking == null) {
                    messages.showError(getString("workorder.error.select_booking"));
                    return;
                }

                workOrderService.createPlannedWorkOrder(selectedBooking.getId());
                messages.showSuccess(getString("workorder.success.workorder_created"));

            }

        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));
            return;
        } catch (Exception e) {
            logger.error("Something Unexpected. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
            return;
        }

        serviceSelections.clear();
        navigateToWorkOrderView();
    }

    @FXML
    private void handleSaveClaim() {
        try {
            WorkOrderCreateDto dto = new WorkOrderCreateDto();
            dto.setDescription(descriptionFieldClaim.getText());

            WorkOrderSummaryDto claim = workOrderService.createClaimWorkOrder(currentWorkOrderId, dto);
            currentWorkOrderId = claim.getId();

            messages.showSuccess(getString("workorder.success.claim_created"));
            navigateToWorkOrderInfoView();
        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));
        } catch (Exception e) {
            logger.error("Could not create claim. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    @FXML
    private void handleUpdateDraft() {
        try {
            workOrderService.updateWorkOrder(currentWorkOrderId, buildDraftDto());
            saveDraftServices();
            messages.showSuccess(getString("workorder.success.workorder_updated"));
            navigateToWorkOrderInfoView();
        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));
        } catch (Exception e) {
            logger.error("Could not update draft. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    @FXML
    private void handleConfirmDraft() {
        try {
            workOrderService.updateWorkOrder(currentWorkOrderId, buildDraftDto());
            saveDraftServices();
            workOrderService.confirmWorkOrder(currentWorkOrderId);
            messages.showSuccess(getString("workorder.success.workorder_confirmed"));
            navigateToWorkOrderInfoView();
        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));
        } catch (Exception e) {
            logger.error("Could not confirm draft. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    private WorkOrderCreateDto buildDraftDto() {
        WorkOrderCreateDto dto = new WorkOrderCreateDto();

        MechanicSummaryDto selectedMechanic = mechanicFieldUpdate.getValue();
        dto.setMechanicId(selectedMechanic != null ? selectedMechanic.getId() : null);
        dto.setPlannedDate(datePickerUpdate.getValue());
        dto.setDescription(descriptionFieldUpdate.getText());

        return dto;
    }

    @FXML
    private void handleCancel() {
        if (messages != null) {
            messages.clearMessage();
        }
        navigateToWorkOrderView();
    }

    private void navigateToWorkOrderView() {
        loadCenterView("/com/wac/autocore/controller/view/WorkOrderView.fxml");
    }

    private void navigateToWorkOrderInfoView() {
        loadCenterView("/com/wac/autocore/controller/view/WorkOrderInfoView.fxml");

    }

    private void loadWorkOrderDetails() {
        if (messages != null) {
            messages.clearMessage();
        }
        try {
            //hämta workorders och ladda labels
            WorkOrderDetailsDto dto = workOrderService.getWorkOrderInfoById(currentWorkOrderId);
            statusLabel.setText(getString("status." + dto.getStatus().name()));
            mechanicLabel.setText(dto.getMechanicName());
            vehicleLabel.setText(dto.getVehicleRegistrationNumber());
            customerLabel.setText(dto.getCustomerName());
            startTimeLabel.setText(dto.getStartTime() != null ? FormatUIUtil.formatTime(dto.getStartTime()) : "-");
            endTimeLabel.setText(dto.getEndTime() != null ? FormatUIUtil.formatTime(dto.getEndTime()) : "-");
            estTimeLabel.setText(dto.getEstimatedDuration() != null ? dto.getEstimatedDuration() + " min" : "-");
            estPriceLabel.setText(dto.getEstimatedPrice() != null ? dto.getEstimatedPrice() + " SEK" : "-");
            bookingIdLabel.setText(dto.getBookingId() != null ? String.valueOf(dto.getBookingId()) : "-");

            updateButtonStates(dto);

            serviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
            servicePriceColumn.setCellValueFactory(new PropertyValueFactory<>("priceAtTime"));
            serviceDurationColumn.setCellValueFactory(new PropertyValueFactory<>("durationAtTime"));
            populateServiceItemsTable(dto);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDraftForm() {
        try {
            WorkOrderDetailsDto dto = workOrderService.getWorkOrderInfoById(currentWorkOrderId);

            vehicleComboUpdate.setItems(FXCollections.observableArrayList(dto.getVehicleRegistrationNumber()));
            vehicleComboUpdate.getSelectionModel().selectFirst();
            vehicleComboUpdate.setDisable(true);

            datePickerUpdate.setValue(dto.getPlannedDate());
            descriptionFieldUpdate.setText(dto.getDescription());

            mechanicFieldUpdate.setItems(FXCollections.observableArrayList(mechanicService.getAllMechanics()));
            mechanicFieldUpdate.setConverter(new StringConverter<MechanicSummaryDto>() {
                @Override
                public String toString(MechanicSummaryDto mechanic) {
                    return mechanic == null ? "" : mechanic.getId() + " - " + mechanic.getName();
                }

                @Override
                public MechanicSummaryDto fromString(String s) {
                    return null;
                }
            });
            if (dto.getMechanicId() != null) {
                for (MechanicSummaryDto mechanic : mechanicFieldUpdate.getItems()) {
                    if (mechanic.getId().equals(dto.getMechanicId())) {
                        mechanicFieldUpdate.getSelectionModel().select(mechanic);
                        break;
                    }
                }
            }
            loadDraftServiceList(dto);
        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));

        } catch (Exception e) {
            logger.error("Could not load draft.", e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    private void loadDraftServiceList(WorkOrderDetailsDto dto) {
        serviceSelections.clear();
        draftServiceRowIds.clear();

        for (WorkOrderServiceItemDto row : dto.getServiceItems()) {
            draftServiceRowIds.put(row.getServiceItemId(), row.getId());
        }

        ObservableList<ServiceItemSummaryDto> allServices =
                FXCollections.observableArrayList(serviceItemService.getAllServiceItems());

        for (ServiceItemSummaryDto item : allServices) {
            boolean onWorkOrder = draftServiceRowIds.containsKey(item.getId());
            serviceSelections.put(item.getId(), new SimpleBooleanProperty(onWorkOrder));
        }

        servicesListViewUpdate.setCellFactory(CheckBoxListCell.forListView(
                item -> serviceSelections.get(item.getId()),
                new StringConverter<ServiceItemSummaryDto>() {
                    @Override
                    public String toString(ServiceItemSummaryDto item) { // Ändrat från ServiceItem till ServiceItemSummaryDto
                        if (item == null) {
                            return "";
                        }
                        return item.getName() + " — " + item.getPrice() + " SEK — " + item.getEstimatedMinutes() + " min";
                    }

                    @Override
                    public ServiceItemSummaryDto fromString(String text) {
                        return null;
                    }
                }
        ));
        servicesListViewUpdate.setItems(allServices);
    }
    private void saveDraftServices() {
        for (ServiceItemSummaryDto item : servicesListViewUpdate.getItems()) {
            boolean checked = serviceSelections.get(item.getId()).get();
            Long rowId = draftServiceRowIds.get(item.getId());

            if (checked && rowId == null) {
                workOrderService.addServiceItemToWorkOrder(currentWorkOrderId, item);
            } else if (!checked && rowId != null) {
                workOrderService.removeServiceItemFromWorkOrder(currentWorkOrderId, rowId);
            }
        }
    }

    // ---------------------------------------------------------
    // NEW DROP-IN
    // ---------------------------------------------------------
// ---------------------------------------------------------
// NEW DROP-IN
// ---------------------------------------------------------
    private void loadDropInForm() {
        // 1. Ladda fordon med VehicleDto
        vehicleComboDropIn.setItems(FXCollections.observableArrayList(vehicleService.getAllVehicles()));
        vehicleComboDropIn.setConverter(new StringConverter<VehicleSummaryDto>() {
            @Override
            public String toString(VehicleSummaryDto vehicle) {
                return vehicle == null ? "" : vehicle.getRegistrationNumber() + " - " + vehicle.getBrand()
                                              + " " + vehicle.getModel();
                // Obs: Om VehicleDto inte har kundnamn direkt kan man lägga till det,
                // annars räcker märke/modell/reggnr utmärkt här!
            }

            @Override
            public VehicleSummaryDto fromString(String s) {
                return null;
            }
        });

        // 2. Ladda mekaniker med MechanicSummaryDto
        mechanicFieldDropIn.setItems(FXCollections.observableArrayList(mechanicService.getAllMechanics()));
        mechanicFieldDropIn.setConverter(new StringConverter<MechanicSummaryDto>() {
            @Override
            public String toString(MechanicSummaryDto mechanic) {
                return mechanic == null ? "" : mechanic.getId() + " - " + mechanic.getName();
            }

            @Override
            public MechanicSummaryDto fromString(String s) {
                return null;
            }
        });

        // 3. Ladda tjänster med ServiceItemSummaryDto
        serviceSelections.clear();
        ObservableList<ServiceItemSummaryDto> allServices =
                FXCollections.observableArrayList(serviceItemService.getAllServiceItems()); // Antar att denna returnerar DTO nu

        for (ServiceItemSummaryDto item : allServices) {
            serviceSelections.put(item.getId(), new SimpleBooleanProperty(false));
        }

        servicesListViewDropIn.setCellFactory(CheckBoxListCell.forListView(
                item -> serviceSelections.get(item.getId()),
                new StringConverter<ServiceItemSummaryDto>() {
                    @Override
                    public String toString(ServiceItemSummaryDto item) {
                        return item.getName() + " — " + item.getPrice() + " SEK — " + item.getEstimatedMinutes() + " min";
                    }

                    @Override
                    public ServiceItemSummaryDto fromString(String text) {
                        return null;
                    }
                }
        ));
        servicesListViewDropIn.setItems(allServices);
    }


    @FXML
    private void handleSaveDropIn() {
        try {
            VehicleSummaryDto vehicle = vehicleComboDropIn.getValue();
            if (vehicle == null) {
                messages.showError(getString("workorder.error.select_vehicle"));
                return;
            }

            WorkOrderCreateDto dto = new WorkOrderCreateDto();
            dto.setVehicleId(vehicle.getId());

            MechanicSummaryDto selectedMechanic = mechanicFieldDropIn.getValue();
            dto.setMechanicId(selectedMechanic != null ? selectedMechanic.getId() : null);

            dto.setPlannedDate(datePickerDropIn.getValue());
            dto.setDescription(descriptionFieldDropIn.getText());

            List<Long> selectedServicesIds = servicesListViewDropIn.getItems().stream()
                    .map(ServiceItemSummaryDto::getId)
                    .filter(id -> serviceSelections.get(id).get())
                    .collect(Collectors.toList());
            dto.setServiceItemsIds(selectedServicesIds);

            workOrderService.createDropInWorkOrder(dto);
            messages.showSuccess(getString("workorder.success.dropin_created"));
            navigateToWorkOrderView();

        } catch (BusinessException e) {
            messages.showError(getString(e.getMessage()));
        } catch (Exception e) {
            logger.error("Could not create drop-in. {}", e.getMessage(), e);
            messages.showError(getString("workorder.error.unexpected"));
        }
    }

    private void populateServiceItemsTable(WorkOrderDetailsDto dto) {
        if (dto != null && dto.getServiceItems() != null) {

            List<WorkOrderServiceItemDto> dtoList = dto.getServiceItems();

            ObservableList<WorkOrderServiceItemDto> observableList = FXCollections.observableArrayList(dtoList);

            serviceItemTable.getItems().setAll(observableList);
        }
    }

    private void updateButtonStates(WorkOrderDetailsDto dto) {
        if (startButton == null) {
            return;
        }

        WorkOrderState status = dto.getStatus();
        boolean hasBooking = dto.getBookingId() != null;

        startButton.setDisable(!status.canStart());
        completeButton.setDisable(!status.canComplete());
        confirmButton.setDisable(!status.canConfirm());
        cancelWorkOrderButton.setDisable(!status.canCancel());
        updateButton.setDisable(!status.canEdit() || hasBooking);
        claimsButton.setDisable(status != WorkOrderState.COMPLETED);
    }

    public void handleUpdate() {
        if (messages != null) {
            messages.clearMessage();
        }
        navigateToUpdateDraftView();
    }

    public void handleClaims() {
        if (messages != null) {
            messages.clearMessage();
        }

        navigateToClaimsView();
    }

    private void navigateToClaimsView() {
        loadCenterView("/com/wac/autocore/controller/view/NewClaimsView.fxml");
    }

    private void navigateToUpdateDraftView() {
        loadCenterView("/com/wac/autocore/controller/view/UpdateWorkOrderDraftView.fxml");

    }
}