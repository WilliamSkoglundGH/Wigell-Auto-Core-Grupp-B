package com.wac.autocore.dto.serviceItem;

import java.math.BigDecimal;

 public class ServiceItemSummaryDto {

        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private int estimatedMinutes;

        protected ServiceItemSummaryDto() {}

        public ServiceItemSummaryDto(Long id, String name, String description, BigDecimal price, int estimatedMinutes) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.estimatedMinutes = estimatedMinutes;
        }

        // Getters och Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public int getEstimatedMinutes() {
            return estimatedMinutes;
        }

        public void setEstimatedMinutes(int estimatedMinutes) {
            this.estimatedMinutes = estimatedMinutes;
        }
    }

