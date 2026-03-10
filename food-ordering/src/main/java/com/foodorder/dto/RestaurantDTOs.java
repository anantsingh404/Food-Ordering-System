package com.foodorder.dto;

import com.foodorder.enums.SelectionStrategy;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

// ── Restaurant DTOs ────────────────────────────────────────────────────────────

public class RestaurantDTOs {

    @Data
    public static class OnboardRequest {
        @NotBlank(message = "Restaurant name is required")
        private String name;

        @Positive(message = "maxOrders must be positive")
        private int maxOrders;

        @NotEmpty(message = "Initial menu must have at least one item")
        private Map<String, BigDecimal> menu;

        @DecimalMin(value = "0.0") @DecimalMax(value = "5.0")
        private double rating;
    }

    @Data
    public static class MenuUpdateRequest {
        @NotNull(message = "Operation is required (ADD or UPDATE)")
        private MenuOperation operation;

        @NotEmpty(message = "Menu updates cannot be empty")
        private Map<String, BigDecimal> items;
    }

    public enum MenuOperation { ADD, UPDATE }

    @Data
    public static class RestaurantResponse {
        private String id;
        private String name;
        private double rating;
        private int maxOrders;
        private int availableCapacity;
        private Map<String, BigDecimal> menu;
    }
}
