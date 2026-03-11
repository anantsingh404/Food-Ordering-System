# Food Ordering System — Spring Boot

## Architecture

```
com.foodorder
├── domain/                  ← Pure domain objects
│   ├── Restaurant.java      ← Owns capacity management + menu
│   ├── Order.java           ← Owns state-machine transitions
│   ├── MenuItem.java
│   └── OrderItem.java
├── enums/
│   ├── OrderStatus.java     ← PLACED → ACCEPTED → COMPLETED | REJECTED
│   └── SelectionStrategy.java
├── repository/              ← In-memory ConcurrentHashMap stores
│   ├── RestaurantRepository.java
│   └── OrderRepository.java
├── strategy/                ← Strategy Pattern (pluggable!)
│   ├── RestaurantSelectionStrategy.java   ← Interface
│   ├── LowestCostStrategy.java
│   ├── HighestRatingStrategy.java
│   └── StrategyRegistry.java             ← Auto-discovers via Spring DI
├── service/
│   ├── RestaurantService.java
│   └── OrderService.java
├── controller/
│   ├── RestaurantController.java
│   └── OrderController.java
├── dto/
│   ├── RestaurantDTOs.java
│   └── OrderDTOs.java
├── config/
│   └── GlobalExceptionHandler.java       ← @RestControllerAdvice
└── FoodOrderingApplication.java          ← CommandLineRunner demo
```

## Design Patterns Used

| Pattern | Where |
|---------|-------|
| **Strategy** | `RestaurantSelectionStrategy` + `StrategyRegistry` — add new strategies with zero changes to existing code |
| **Repository** | `RestaurantRepository`, `OrderRepository` — abstracts in-memory storage |
| **Factory (via Spring DI)** | `StrategyRegistry` auto-collects all strategy beans |
| **State Machine** | `Order.java` — strict transition guards (PLACED→ACCEPTED→COMPLETED) |
| **DTO** | Separates API contract from domain objects |
| **Global Error Handler** | `GlobalExceptionHandler` — single place maps exceptions → HTTP codes |

## Business Rules Enforced

- ✅ All items must be fulfillable by a **single** restaurant
- ✅ Restaurant capacity (maxOrders) is enforced atomically (`synchronized reserveCapacity()`)
- ✅ ACCEPTED orders **cannot** be cancelled
- ✅ Menu items **cannot** be deleted (ADD/UPDATE only)
- ✅ Completed orders **release** restaurant capacity
- ✅ Orders are immutable once placed

## Running

```bash
mvn spring-boot:run
```

The `CommandLineRunner` in `FoodOrderingApplication` runs all 5 spec test cases automatically on startup.

## REST API

### Restaurants

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/restaurants` | Onboard new restaurant |
| GET | `/api/restaurants` | List all restaurants |
| GET | `/api/restaurants/{name}` | Get restaurant by name |
| PATCH | `/api/restaurants/{name}/menu` | Add or update menu items |

### Orders

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/orders` | Place an order |
| GET | `/api/orders` | List all orders (optional `?status=ACCEPTED`) |
| GET | `/api/orders/{orderId}` | Get order by ID |
| PATCH | `/api/orders/{orderId}/complete?restaurantName=R3` | Mark order as completed |

## Sample API Requests

### Onboard Restaurant
```json
POST /api/restaurants
{
  "name": "R1",
  "maxOrders": 5,
  "rating": 4.5,
  "menu": {
    "Veg Biryani": 100,
    "Chicken Biryani": 150
  }
}
```

### Place Order
```json
POST /api/orders
{
  "customerName": "Ashwin",
  "items": { "Idli": 3, "Dosa": 1 },
  "selectionStrategy": "LOWEST_COST"
}
```

### Update Menu (Add items)
```json
PATCH /api/restaurants/R1/menu
{
  "operation": "ADD",
  "items": { "Chicken65": 250 }
}
```

### Complete Order
```
PATCH /api/orders/ORD-ABC123/complete?restaurantName=R3
```

## Adding a New Selection Strategy

1. Create a class implementing `RestaurantSelectionStrategy`
2. Annotate it with `@Component`
3. Add the corresponding value to `SelectionStrategy` enum
4. That's it — `StrategyRegistry` picks it up automatically via Spring DI

```java
@Component
public class RandomStrategy implements RestaurantSelectionStrategy {
    @Override
    public Optional<Restaurant> select(List<Restaurant> eligible, Map<String, Integer> items) {
        if (eligible.isEmpty()) return Optional.empty();
        return Optional.of(eligible.get(new Random().nextInt(eligible.size())));
    }
    
    @Override
    public String strategyName() { return SelectionStrategy.RANDOM.name(); }
}
```
