package com.wac.autocore.mapper;

import com.wac.autocore.dto.serviceItem.ServiceItemDto;
import com.wac.autocore.model.ServiceItem;
import org.springframework.stereotype.Component;

@Component
public final class ServiceItemMapper {


    public static ServiceItemDto toServiceItemDto(ServiceItem sI) {
        return new ServiceItemDto(
                sI.getId(),
                sI.getName(),
                sI.getDescription(),
                sI.getPrice(),
                sI.getEstimatedMinutes());
    }

    public static ServiceItem toServiceItem(ServiceItemDto serviceItemDto) {



    }
}
