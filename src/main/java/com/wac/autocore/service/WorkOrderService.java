package com.wac.autocore.service;

import com.wac.autocore.dto.workorder.*;
import com.wac.autocore.security.RequiresRole;

import java.util.List;

import static com.wac.autocore.model.enums.UserRole.ADMIN;
import static com.wac.autocore.model.enums.UserRole.MECHANIC;

/**
 * Behörighet kontrolleras av SecurityProxy (se SecuredServiceConfig), inte av Spring Security.
 * @RequiresRole utan roller = inloggad användare räcker.
 */
public interface WorkOrderService {

    @RequiresRole
    List<WorkOrderSummaryDto> getAllWorkOrders();

    @RequiresRole
    List<WorkOrderForInvoiceDto> getAllWorkOrdersForInvoice();

    @RequiresRole
    WorkOrderResponseDto getWorkOrderById(long id);

    @RequiresRole({ADMIN, MECHANIC})
    WorkOrderSummaryDto createPlannedWorkOrder(Long bookingId);

    @RequiresRole({ADMIN, MECHANIC})
    WorkOrderSummaryDto createDropInWorkOrder(WorkOrderCreateDto dto);

    @RequiresRole({ADMIN, MECHANIC})
    WorkOrderSummaryDto createClaimWorkOrder(Long originalWorkOrderId, WorkOrderCreateDto dto);

    @RequiresRole({ADMIN, MECHANIC})
    void startWorkOrder(Long workOrderId);

    @RequiresRole({ADMIN, MECHANIC})
    void completeWorkOrder(Long workOrderId);

    @RequiresRole
    WorkOrderDetailsDto getWorkOrderInfoById(Long workOrderId);

    @RequiresRole({ADMIN, MECHANIC})
    void confirmWorkOrder(Long workOrderId);

    @RequiresRole({ADMIN, MECHANIC})
    void cancelWorkOrder(Long workOrderId);

    @RequiresRole({ADMIN, MECHANIC})
    void addServiceItemToWorkOrder(Long workOrderId, Long serviceItemId);

    @RequiresRole({ADMIN, MECHANIC})
    void removeServiceItemFromWorkOrder(Long workOrderId, Long workOrderServiceItemId);

    @RequiresRole({ADMIN, MECHANIC})
    void updateWorkOrder(Long workOrderId, WorkOrderCreateDto dto);
}
