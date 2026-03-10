package com.foodorder.domain;

import lombok.Getter;

/**
 * Represents a single line item in an order (item + quantity).
 */
@Getter
public class OrderItem {

    private final String itemName;
    private final int quantity;

    public OrderItem(String itemName, int quantity) {
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("Item name cannot be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive for item: " + itemName);
        }
        this.itemName = itemName;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("OrderItem{name='%s', qty=%d}", itemName, quantity);
    }
}
