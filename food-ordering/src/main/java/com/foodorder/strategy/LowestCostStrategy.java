package com.foodorder.strategy;

import com.foodorder.domain.Restaurant;
import com.foodorder.enums.SelectionStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Selects the restaurant that produces the lowest total bill for the order.
 */
@Component
public class LowestCostStrategy implements RestaurantSelectionStrategy {

    @Override
    public Optional<Restaurant> select(List<Restaurant> eligibleRestaurants, Map<String, Integer> requestedItems) {
        return eligibleRestaurants.stream()
                .min(Comparator.comparing(r -> r.calculateBill(requestedItems)));
    }

    @Override
    public String strategyName() {
        return SelectionStrategy.LOWEST_COST.name();
    }
}
