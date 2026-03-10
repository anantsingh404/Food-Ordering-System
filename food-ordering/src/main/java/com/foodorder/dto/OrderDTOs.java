package com.foodorder.dto;

import com.foodorder.enums.OrderStatus;
import com.foodorder.enums.SelectionStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class OrderDTOs {

    @Data
    public static class PlaceOrderRequest {
        @NotBlank(message = "Customer name is required")
        private String customerName;

        @NotEmpty(message = "Items cannot be empty")
        private Map<String, Integer> items;   // item name -> quantity

        @NotNull(message = "Selection strategy is required")
        private SelectionStrategy selectionStrategy;
    }

    @Data
    public static class OrderResponse {
        private String orderId;
        private String customerName;
        private OrderStatus status;
        private String assignedRestaurantName;
        private BigDecimal totalBill;
        private LocalDateTime placedAt;
        private Map<String, Integer> items;
        private String message;
    }
}
