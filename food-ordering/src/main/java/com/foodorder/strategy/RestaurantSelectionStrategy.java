package com.foodorder.strategy;

import com.foodorder.domain.Restaurant;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Strategy Pattern interface for selecting a restaurant from eligible candidates.
 */
public interface RestaurantSelectionStrategy {
    Optional<Restaurant> select(List<Restaurant> eligibleRestaurants, Map<String, Integer> requestedItems);
    String strategyName();
}
