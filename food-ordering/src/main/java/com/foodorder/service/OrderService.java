package com.foodorder.service;

import com.foodorder.domain.Order;
import com.foodorder.domain.OrderItem;
import com.foodorder.domain.Restaurant;
import com.foodorder.enums.OrderStatus;
import com.foodorder.enums.SelectionStrategy;
import com.foodorder.exception.InvalidOrderStateException;
import com.foodorder.exception.OrderNotAssignableException;
import com.foodorder.exception.OrderNotFoundException;
import com.foodorder.repository.OrderRepository;
import com.foodorder.repository.RestaurantRepository;
import com.foodorder.strategy.RestaurantSelectionStrategy;
import com.foodorder.strategy.StrategyRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business logic for order placement, assignment, and lifecycle management.
 *
 * Key rules enforced:
 *  - All items must be fulfillable by a single restaurant
 *  - Restaurant must have capacity
 *  - ACCEPTED orders cannot be cancelled
 *  - COMPLETED status frees up restaurant capacity
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final StrategyRegistry strategyRegistry;

    // ── Place Order ────────────────────────────────────────────────────────────

    /**
     * Place and auto-assign an order using the specified selection strategy.
     * Atomically reserves capacity on the chosen restaurant.
     */
    public Order placeOrder(String customerName, Map<String, Integer> requestedItems,
                            SelectionStrategy selectionStrategy) {
        validateOrderItems(requestedItems);

        List<OrderItem> orderItems = requestedItems.entrySet().stream()
                .map(e -> new OrderItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Order order = new Order(orderId, customerName, orderItems, selectionStrategy);
        orderRepository.save(order);

        // Find eligible restaurants: can fulfill ALL items AND have capacity
        List<Restaurant> eligible = restaurantRepository.findAll().stream()
                .filter(r -> r.canFulfill(requestedItems))
                .filter(Restaurant::hasCapacity)
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            order.reject("No restaurant can fulfill this order");
            log.warn("Order {} rejected: no eligible restaurant found", orderId);
            throw new OrderNotAssignableException(
                    "Cannot assign order for customer '" + customerName + "': " +
                    "No restaurant can fulfill all items or all restaurants are at capacity.");
        }

        // Apply selection strategy
        RestaurantSelectionStrategy strategy = strategyRegistry.getStrategy(selectionStrategy);
        Optional<Restaurant> selected = strategy.select(eligible, requestedItems);

        if (selected.isEmpty()) {
            order.reject("Strategy could not select a restaurant");
            throw new OrderNotAssignableException("Strategy failed to select a restaurant");
        }

        Restaurant restaurant = selected.get();

        // Atomic capacity reservation — handles race conditions
        if (!restaurant.reserveCapacity()) {
            order.reject("Restaurant at full capacity");
            throw new OrderNotAssignableException(
                    "Restaurant '" + restaurant.getName() + "' is now at full capacity. Please retry.");
        }

        BigDecimal bill = restaurant.calculateBill(requestedItems);
        order.accept(restaurant.getId(), restaurant.getName(), bill);

        log.info("Order {} assigned to {} | Bill: {} | Strategy: {}",
                orderId, restaurant.getName(), bill, selectionStrategy);

        return order;
    }

    // ── Complete Order ─────────────────────────────────────────────────────────

    /**
     * Mark an accepted order as completed. Frees restaurant capacity.
     * Only the assigned restaurant can complete the order.
     */
    public Order completeOrder(String orderId, String restaurantName) {
        Order order = findByIdOrThrow(orderId);

        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new InvalidOrderStateException(
                    "Order " + orderId + " is in state " + order.getStatus() + ". Only ACCEPTED orders can be completed.");
        }

        // Verify the restaurant completing is the one assigned
        Restaurant restaurant = restaurantRepository.findAll().stream()
                .filter(r -> r.getName().equalsIgnoreCase(restaurantName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + restaurantName));

        if (!restaurant.getId().equals(order.getAssignedRestaurantId())) {
            throw new InvalidOrderStateException(
                    "Restaurant '" + restaurantName + "' is not assigned to order " + orderId);
        }

        order.complete();
        restaurant.releaseCapacity();

        log.info("Order {} completed by {}. Capacity released: {}/{}",
                orderId, restaurantName, restaurant.getActiveOrders().get(), restaurant.getMaxOrders());

        return order;
    }

    // ── Queries ────────────────────────────────────────────────────────────────

    public Order findByIdOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    public List<Order> findAll() {
        return (List<Order>) orderRepository.findAll();
    }

    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    private void validateOrderItems(Map<String, Integer> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        items.forEach((name, qty) -> {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Item name cannot be blank");
            if (qty == null || qty <= 0) throw new IllegalArgumentException("Quantity must be positive for: " + name);
        });
    }
}
