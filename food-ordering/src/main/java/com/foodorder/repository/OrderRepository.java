package com.foodorder.repository;

import com.foodorder.domain.Order;
import com.foodorder.enums.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for Orders.
 */
@Repository
public class OrderRepository {

    private final Map<String, Order> store = new ConcurrentHashMap<>();

    public Order save(Order order) {
        store.put(order.getOrderId(), order);
        return order;
    }

    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    public List<Order> findByRestaurantId(String restaurantId) {
        return store.values().stream()
                .filter(o -> restaurantId.equals(o.getAssignedRestaurantId()))
                .collect(Collectors.toList());
    }

    public List<Order> findByStatus(OrderStatus status) {
        return store.values().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
    }

    public Collection<Order> findAll() {
        return Collections.unmodifiableCollection(store.values());
    }

    public void clear() {
        store.clear();
    }
}
