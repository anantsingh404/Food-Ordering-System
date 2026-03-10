package com.foodorder.strategy;

import com.foodorder.enums.SelectionStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry that holds all pluggable selection strategies.
 * New strategies are automatically picked up via Spring DI — no code change needed here.
 */
@Component
public class StrategyRegistry {

    private final Map<String, RestaurantSelectionStrategy> strategies;

    public StrategyRegistry(List<RestaurantSelectionStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(RestaurantSelectionStrategy::strategyName, Function.identity()));
    }

    public RestaurantSelectionStrategy getStrategy(SelectionStrategy strategy) {
        RestaurantSelectionStrategy impl = strategies.get(strategy.name());
        if (impl == null) {
            throw new IllegalArgumentException("No implementation found for strategy: " + strategy);
        }
        return impl;
    }
}
