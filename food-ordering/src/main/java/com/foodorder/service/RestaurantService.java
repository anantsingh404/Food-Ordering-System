package com.foodorder.service;

import com.foodorder.domain.MenuItem;
import com.foodorder.domain.Restaurant;
import com.foodorder.dto.RestaurantDTOs;
import com.foodorder.exception.DuplicateRestaurantException;
import com.foodorder.exception.RestaurantNotFoundException;
import com.foodorder.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Business logic for restaurant management.
 * Separation of concerns: domain logic stays in domain objects, orchestration here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    // ── Onboarding ─────────────────────────────────────────────────────────────

    public Restaurant onboardRestaurant(String name, int maxOrders, double rating,
                                        Map<String, BigDecimal> menuItems) {
        if (restaurantRepository.existsByName(name)) {
            throw new DuplicateRestaurantException("Restaurant with name '" + name + "' already exists");
        }

        Map<String, MenuItem> menu = new HashMap<>();
        menuItems.forEach((itemName, price) -> {
            validateMenuItemInput(itemName, price);
            menu.put(itemName, new MenuItem(itemName, price));
        });

        String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Restaurant restaurant = new Restaurant(id, name, maxOrders, rating, menu);
        restaurantRepository.save(restaurant);

        log.info("Onboarded restaurant: {} with id: {}", name, id);
        return restaurant;
    }

    // ── Menu Updates ───────────────────────────────────────────────────────────

    public Restaurant addMenuItems(String restaurantName, Map<String, BigDecimal> newItems) {
        Restaurant restaurant = findByNameOrThrow(restaurantName);
        newItems.forEach((itemName, price) -> {
            validateMenuItemInput(itemName, price);
            restaurant.addMenuItem(itemName, price);
            log.info("Added item '{}' at {} to {}", itemName, price, restaurantName);
        });
        return restaurant;
    }

    public Restaurant updateMenuItemPrices(String restaurantName, Map<String, BigDecimal> updatedItems) {
        Restaurant restaurant = findByNameOrThrow(restaurantName);
        updatedItems.forEach((itemName, price) -> {
            validateMenuItemInput(itemName, price);
            restaurant.updateMenuItemPrice(itemName, price);
            log.info("Updated item '{}' to {} in {}", itemName, price, restaurantName);
        });
        return restaurant;
    }

    // ── Queries ────────────────────────────────────────────────────────────────

    public Restaurant findById(String id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + id));
    }

    public Collection<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    public Restaurant findByNameOrThrow(String name) {
        return restaurantRepository.findByName(name)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found: " + name));
    }

    private void validateMenuItemInput(String itemName, BigDecimal price) {
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("Menu item name cannot be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive for item: " + itemName);
        }
    }
}
