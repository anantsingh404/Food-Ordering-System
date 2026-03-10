package com.foodorder.domain;

import com.foodorder.enums.OrderStatus;
import com.foodorder.enums.SelectionStrategy;
import com.foodorder.exception.InvalidOrderStateException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Immutable order entity once placed. Status transitions are strictly controlled.
 *
 * Allowed transitions:
 *   PLACED → ACCEPTED
 *   PLACED → REJECTED
 *   ACCEPTED → COMPLETED
 *   (ACCEPTED cannot go back to REJECTED or CANCELLED per business rules)
 */
public class Order {

    private final String orderId;
    private final String customerName;
    private final List<OrderItem> items;
    private final SelectionStrategy selectionStrategy;
    private final LocalDateTime placedAt;

    private OrderStatus status;
    private String assignedRestaurantId;
    private String assignedRestaurantName;
    private BigDecimal totalBill;

    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public List<OrderItem> getItems() { return items; }
    public SelectionStrategy getSelectionStrategy() { return selectionStrategy; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public OrderStatus getStatus() { return status; }
    public String getAssignedRestaurantId() { return assignedRestaurantId; }
    public String getAssignedRestaurantName() { return assignedRestaurantName; }
    public BigDecimal getTotalBill() { return totalBill; }

    public Order(String orderId, String customerName, List<OrderItem> items, SelectionStrategy selectionStrategy) {
        validateInputs(orderId, customerName, items, selectionStrategy);
        this.orderId = orderId;
        this.customerName = customerName;
        this.items = Collections.unmodifiableList(items);
        this.selectionStrategy = selectionStrategy;
        this.placedAt = LocalDateTime.now();
        this.status = OrderStatus.PLACED;
    }

    // ── State transitions ──────────────────────────────────────────────────────

    public void accept(String restaurantId, String restaurantName, BigDecimal bill) {
        if (status != OrderStatus.PLACED) {
            throw new InvalidOrderStateException("Cannot accept order in state: " + status);
        }
        this.assignedRestaurantId = restaurantId;
        this.assignedRestaurantName = restaurantName;
        this.totalBill = bill;
        this.status = OrderStatus.ACCEPTED;
    }

    public void complete() {
        if (status != OrderStatus.ACCEPTED) {
            throw new InvalidOrderStateException(
                    "Only ACCEPTED orders can be completed. Current state: " + status);
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void reject(String reason) {
        if (status != OrderStatus.PLACED) {
            throw new InvalidOrderStateException("Cannot reject order in state: " + status);
        }
        this.status = OrderStatus.REJECTED;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    public java.util.Map<String, Integer> getItemsAsMap() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        items.forEach(i -> map.put(i.getItemName(), i.getQuantity()));
        return map;
    }

    private void validateInputs(String orderId, String customerName,
                                List<OrderItem> items, SelectionStrategy strategy) {
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("Order ID cannot be blank");
        if (customerName == null || customerName.isBlank()) throw new IllegalArgumentException("Customer name cannot be blank");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Order must have at least one item");
        if (strategy == null) throw new IllegalArgumentException("Selection strategy must be specified");
    }

    @Override
    public String toString() {
        return String.format("Order{id='%s', customer='%s', status=%s, restaurant='%s', bill=%s}",
                orderId, customerName, status, assignedRestaurantName, totalBill);
    }
}