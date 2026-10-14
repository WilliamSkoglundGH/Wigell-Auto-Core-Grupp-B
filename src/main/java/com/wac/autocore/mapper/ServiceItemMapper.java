package com.wac.autocore.mapper;

import com.wac.autocore.dto.serviceItem.ServiceItemCreateDto;
import com.wac.autocore.dto.serviceItem.ServiceItemSummaryDto;
import com.wac.autocore.model.ServiceItem;
import org.springframework.stereotype.Component;

@Component
public final class ServiceItemMapper {

    public static ServiceItemSummaryDto toServiceItemDto(ServiceItem sI) {
        return new ServiceItemSummaryDto(
                sI.getId(),
                sI.getName(),
                sI.getDescription(),
                sI.getPrice(),
                sI.getEstimatedMinutes());
    }
}
