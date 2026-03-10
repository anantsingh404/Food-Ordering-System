package com.foodorder.repository;

import com.foodorder.domain.Restaurant;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for Restaurants.
 * Uses ConcurrentHashMap for thread safety.
 */
@Repository
public class RestaurantRepository {

    private final Map<String, Restaurant> store = new ConcurrentHashMap<>();

    public Restaurant save(Restaurant restaurant) {
        store.put(restaurant.getId(), restaurant);
        return restaurant;
    }

    public Optional<Restaurant> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Restaurant> findByName(String name) {
        return store.values().stream()
                .filter(r -> r.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public boolean existsByName(String name) {
        return store.values().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(name));
    }

    public Collection<Restaurant> findAll() {
        return Collections.unmodifiableCollection(store.values());
    }

    public void clear() {
        store.clear();
    }
}
