package com.foodorder;

import com.foodorder.domain.Order;
import com.foodorder.enums.SelectionStrategy;
import com.foodorder.exception.OrderNotAssignableException;
import com.foodorder.service.OrderService;
import com.foodorder.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class FoodOrderingApplication implements CommandLineRunner {

    private final RestaurantService restaurantService;
    private final OrderService orderService;

    public static void main(String[] args) {
        SpringApplication.run(FoodOrderingApplication.class, args);
    }

    /**
     * Demo driver — runs all sample test cases from the spec on startup.
     * Easy to modify/comment out for interview demos.
     */
    @Override
    public void run(String... args) {
        log.info("═══════════════════════════════════════════════");
        log.info("  FOOD ORDERING SYSTEM — DEMO");
        log.info("═══════════════════════════════════════════════");

        // ── 1. Onboard Restaurants ─────────────────────────────────────────────
        log.info("\n--- Onboarding Restaurants ---");

        restaurantService.onboardRestaurant("R1", 5, 4.5, Map.of(
                "Veg Biryani", new BigDecimal("100"),
                "Chicken Biryani", new BigDecimal("150")));
        log.info("✓ R1 onboarded");

        restaurantService.onboardRestaurant("R2", 5, 4.0, Map.of(
                "Idli", new BigDecimal("10"),
                "Dosa", new BigDecimal("50"),
                "Veg Biryani", new BigDecimal("80"),
                "Chicken Biryani", new BigDecimal("175")));
        log.info("✓ R2 onboarded");

        restaurantService.onboardRestaurant("R3", 1, 4.9, Map.of(
                "Idli", new BigDecimal("15"),
                "Dosa", new BigDecimal("30"),
                "Gobi Manchurian", new BigDecimal("150"),
                "Chicken Biryani", new BigDecimal("175")));
        log.info("✓ R3 onboarded");

        // ── 2. Update Menus ────────────────────────────────────────────────────
        log.info("\n--- Updating Menus ---");

        restaurantService.addMenuItems("R1", Map.of("Chicken65", new BigDecimal("250")));
        log.info("✓ Added Chicken65 to R1");

        restaurantService.updateMenuItemPrices("R2", Map.of("Chicken Biryani", new BigDecimal("150")));
        log.info("✓ Updated Chicken Biryani price in R2");

        // ── 3. Place Orders ────────────────────────────────────────────────────
        log.info("\n--- Placing Orders ---");

        // Order 01: Ashwin — Lowest Cost → should go to R3 (75 < 80)
        Order order1 = placeOrderSafe("Ashwin",
                Map.of("Idli", 3, "Dosa", 1), SelectionStrategy.LOWEST_COST, "Order 01");
        // Expected: R3 (R2=80, R3=75)

        // Order 02: Harish — Lowest Cost → should go to R2 (R3 is full)
        Order order2 = placeOrderSafe("Harish",
                Map.of("Idli", 3, "Dosa", 1), SelectionStrategy.LOWEST_COST, "Order 02");
        // Expected: R2 (R3 capacity full)

        // Order 03: Shruthi — Highest Rating → should go to R1 (rating 4.5 > R2's 4.0)
        Order order3 = placeOrderSafe("Shruthi",
                Map.of("Veg Biryani", 3, "Dosa", 1), SelectionStrategy.HIGHEST_RATING, "Order 03");
        // Expected: R1 (R1 rating 4.5 > R2 rating 4.0)

        // ── 4. Complete Order 01 ───────────────────────────────────────────────
        log.info("\n--- R3 marks Order 01 as COMPLETED ---");
        if (order1 != null) {
            orderService.completeOrder(order1.getOrderId(), "R3");
            log.info("✓ Order 01 completed → R3 capacity freed");
        }

        // ── 5. Order 04: Harish again — should now go to R3 (capacity freed) ──
        Order order4 = placeOrderSafe("Harish",
                Map.of("Idli", 3, "Dosa", 1), SelectionStrategy.LOWEST_COST, "Order 04");
        // Expected: R3 (now free, R3=75 < R2=80)

        // ── 6. Order 05: Impossible order ─────────────────────────────────────
        log.info("\n--- Order 05: Diya (should FAIL - Paneer Tikka not available) ---");
        placeOrderSafe("Diya",
                Map.of("Idli", 3, "Paneer Tikka", 1), SelectionStrategy.LOWEST_COST, "Order 05");

        // ── Summary ───────────────────────────────────────────────────────────
        log.info("\n═══════════════════════════════════════════════");
        log.info("  DEMO COMPLETE — API available at http://localhost:8080");
    }

    private Order placeOrderSafe(String customer, Map<String, Integer> items,
                                  SelectionStrategy strategy, String label) {
        log.info("\n[{}] Customer: {}, Strategy: {}", label, customer, strategy);
        try {
            Order order = orderService.placeOrder(customer, items, strategy);
            log.info("[{}] ✓ Assigned to: {} | Bill: INR {}", label, order.getAssignedRestaurantName(), order.getTotalBill());
            return order;
        } catch (OrderNotAssignableException ex) {
            log.warn("[{}] ✗ Cannot assign order: {}", label, ex.getMessage());
            return null;
        }
    }
}
