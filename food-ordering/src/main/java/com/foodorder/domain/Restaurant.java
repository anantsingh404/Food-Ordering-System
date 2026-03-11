package com.foodorder.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
@Getter
public class Restaurant {

    private final String id;
    private final String name;
    private final int maxOrders;
    private final double rating;
    private final Map<String, MenuItem> menu;          // item name -> MenuItem
    private final AtomicInteger activeOrders;          // current processing count

    public Restaurant(String id, String name, int maxOrders, double rating, Map<String, MenuItem> initialMenu) {
        validateInputs(id, name, maxOrders, rating);
        this.id = id;
        this.name = name;
        this.maxOrders = maxOrders;
        this.rating = rating;
        this.menu = new ConcurrentHashMap<>(initialMenu);
        this.activeOrders = new AtomicInteger(0);
    }

    // ── Menu operations ────────────────────────────────────────────────────────

    /**
     * Add a new item to the menu. Throws if item already exists (use updateMenuItem).
     */
    public void addMenuItem(String itemName, BigDecimal price) {
        if (menu.containsKey(itemName)) {
            throw new IllegalArgumentException(
                    "Item '" + itemName + "' already exists in " + name + "'s menu. Use update to change price.");
        }
        menu.put(itemName, new MenuItem(itemName, price));
    }

    /**
     * Update price of an existing menu item.
     */
    public void updateMenuItemPrice(String itemName, BigDecimal newPrice) {
        MenuItem item = menu.get(itemName);
        if (item == null) {
            throw new IllegalArgumentException("Item '" + itemName + "' not found in " + name + "'s menu");
        }
        item.updatePrice(newPrice);
    }

    /**
     * Check if the restaurant can fulfill ALL requested items.
     */
    public boolean canFulfill(Map<String, Integer> requestedItems) {
        return requestedItems.keySet().stream()
                .allMatch(menu::containsKey);
    }

    /**
     * Calculate total bill for the requested items.
     */
    public BigDecimal calculateBill(Map<String, Integer> requestedItems) {
        return requestedItems.entrySet().stream()
                .map(e -> menu.get(e.getKey()).getPrice()
                        .multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ── Capacity management ────────────────────────────────────────────────────

    public boolean hasCapacity() {
        return activeOrders.get() < maxOrders;
    }

    /**
     * Atomically increment active orders if capacity available.
     * Returns true if reservation succeeded.
     */
    public synchronized boolean reserveCapacity() {
        if (activeOrders.get() < maxOrders) {
            activeOrders.incrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * Release a slot when order is COMPLETED.
     */
    public void releaseCapacity() {
        activeOrders.updateAndGet(v -> Math.max(0, v - 1));
    }

    public int getAvailableCapacity() {
        return maxOrders - activeOrders.get();
    }

    public Map<String, MenuItem> getMenu() {
        return Collections.unmodifiableMap(menu);
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    private void validateInputs(String id, String name, int maxOrders, double rating) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Restaurant ID cannot be blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Restaurant name cannot be blank");
        if (maxOrders <= 0) throw new IllegalArgumentException("maxOrders must be > 0, got: " + maxOrders);
        if (rating < 0 || rating > 5) throw new IllegalArgumentException("Rating must be between 0 and 5, got: " + rating);
    }

    @Override
    public String toString() {
        return String.format("Restaurant{id='%s', name='%s', rating=%.1f, activeOrders=%d/%d}",
                id, name, rating, activeOrders.get(), maxOrders);
    }
}
