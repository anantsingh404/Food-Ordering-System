package com.foodorder.strategy;

import com.foodorder.domain.Restaurant;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Strategy Pattern interface for selecting a restaurant from eligible candidates.
 * Implement this to add new selection strategies without modifying existing code (Open/Closed Principle).
 */
public interface RestaurantSelectionStrategy {

    /**
     * Select the best restaurant from a list of eligible candidates.
     *
     * @param eligibleRestaurants restaurants that can fulfill the order and have capacity
     * @param requestedItems      items and quantities in the order (needed for bill calculation)
     * @return the selected restaurant, or empty if none available
     */
    Optional<Restaurant> select(List<Restaurant> eligibleRestaurants, Map<String, Integer> requestedItems);

    /**
     * Returns the strategy name for registration and lookup.
     */
    String strategyName();
}
