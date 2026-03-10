# Food Ordering System

A backend system I built to practice system design and clean code in Java Spring Boot.

The idea is simple — customers place food orders, and the system automatically picks 
the best restaurant based on either lowest price or highest rating.

## What it does

- Add restaurants with menus and ratings
- Update menu prices or add new items (no deletions allowed by design)
- Place orders — system auto-assigns to best restaurant
- Restaurants have a max order limit — no over-booking
- Mark orders as completed to free up restaurant capacity

## Tech used

- Java 17
- Spring Boot 3.2
- All data stored in-memory (no database)

## How to run

Open in IntelliJ, let Maven download dependencies, then run `FoodOrderingApplication.java`.

App starts on `http://localhost:8080`

On startup it automatically runs through a demo with 3 restaurants and 5 orders 
so you can see everything working in the console.

## API endpoints

| Method | URL | What it does |
|--------|-----|--------------|
| POST | `/api/restaurants` | Add a new restaurant |
| GET | `/api/restaurants` | List all restaurants |
| PATCH | `/api/restaurants/{name}/menu` | Update menu |
| POST | `/api/orders` | Place an order |
| GET | `/api/orders` | List all orders |
| PATCH | `/api/orders/{id}/complete` | Mark order as done |

## Example — place an order
```json
POST /api/orders
{
  "customerName": "Ashwin",
  "items": { "Idli": 3, "Dosa": 1 },
  "selectionStrategy": "LOWEST_COST"
}
```

## Selection strategies

- `LOWEST_COST` — picks the restaurant with the cheapest total bill
- `HIGHEST_RATING` — picks the highest rated restaurant that can fulfill the order

Adding a new strategy is easy — just implement the `RestaurantSelectionStrategy` 
interface and Spring picks it up automatically.
