package com.foodorder.domain;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Represents a single item on a restaurant's menu.
 * Price can be updated but item cannot be deleted.
 */
@Getter
public class MenuItem {

    private final String name;
    private BigDecimal price;

    public MenuItem(String name, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Menu item name cannot be null or blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Menu item price must be positive");
        }
        this.name = name;
        this.price = price;
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = newPrice;
    }

    @Override
    public String toString() {
        return String.format("MenuItem{name='%s', price=%.2f}", name, price);
    }
}
