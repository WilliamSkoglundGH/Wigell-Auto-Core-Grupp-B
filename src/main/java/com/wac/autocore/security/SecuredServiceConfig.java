package com.wac.autocore.security;

import com.wac.autocore.service.WorkOrderService;
import com.wac.autocore.service.WorkOrderServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Exponerar de proxy-skyddade servicerna. Controllers som injicerar interfacet
 * får proxyn (@Primary); Impl-bönan ligger kvar bakom för @Transactional.
 */
@Configuration
public class SecuredServiceConfig {

    @Bean
    @Primary
    public WorkOrderService securedWorkOrderService(WorkOrderServiceImpl workOrderService) {
        return SecurityProxy.protect(WorkOrderService.class, workOrderService);
    }
}
