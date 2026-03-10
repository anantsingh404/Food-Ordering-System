package com.foodorder.strategy;

import com.foodorder.domain.Restaurant;
import com.foodorder.enums.SelectionStrategy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Selects the restaurant with the highest rating.
 */
@Component
public class HighestRatingStrategy implements RestaurantSelectionStrategy {

    @Override
    public Optional<Restaurant> select(List<Restaurant> eligibleRestaurants, Map<String, Integer> requestedItems) {
        return eligibleRestaurants.stream()
                .max(Comparator.comparingDouble(Restaurant::getRating));
    }

    @Override
    public String strategyName() {
        return SelectionStrategy.HIGHEST_RATING.name();
    }
}
