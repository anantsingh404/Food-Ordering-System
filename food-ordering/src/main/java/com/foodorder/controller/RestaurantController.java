package com.foodorder.controller;

import com.foodorder.domain.Restaurant;
import com.foodorder.dto.RestaurantDTOs;
import com.foodorder.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    /** POST /api/restaurants — Onboard a new restaurant */
    @PostMapping
    public ResponseEntity<RestaurantDTOs.RestaurantResponse> onboard(
            @Valid @RequestBody RestaurantDTOs.OnboardRequest request) {

        Restaurant restaurant = restaurantService.onboardRestaurant(
                request.getName(), request.getMaxOrders(), request.getRating(), request.getMenu());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(restaurant));
    }

    /** GET /api/restaurants — List all restaurants */
    @GetMapping
    public ResponseEntity<Collection<RestaurantDTOs.RestaurantResponse>> listAll() {
        Collection<RestaurantDTOs.RestaurantResponse> responses = restaurantService.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /** GET /api/restaurants/{name} — Get a restaurant by name */
    @GetMapping("/{name}")
    public ResponseEntity<RestaurantDTOs.RestaurantResponse> getByName(@PathVariable String name) {
        Restaurant restaurant = restaurantService.findByNameOrThrow(name);
        return ResponseEntity.ok(toResponse(restaurant));
    }

    /** PATCH /api/restaurants/{name}/menu — Add or update menu items */
    @PatchMapping("/{name}/menu")
    public ResponseEntity<RestaurantDTOs.RestaurantResponse> updateMenu(
            @PathVariable String name,
            @Valid @RequestBody RestaurantDTOs.MenuUpdateRequest request) {

        Restaurant restaurant;
        if (request.getOperation() == RestaurantDTOs.MenuOperation.ADD) {
            restaurant = restaurantService.addMenuItems(name, request.getItems());
        } else {
            restaurant = restaurantService.updateMenuItemPrices(name, request.getItems());
        }
        return ResponseEntity.ok(toResponse(restaurant));
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private RestaurantDTOs.RestaurantResponse toResponse(Restaurant r) {
        RestaurantDTOs.RestaurantResponse resp = new RestaurantDTOs.RestaurantResponse();
        resp.setId(r.getId());
        resp.setName(r.getName());
        resp.setRating(r.getRating());
        resp.setMaxOrders(r.getMaxOrders());
        resp.setAvailableCapacity(r.getAvailableCapacity());
        Map<String, BigDecimal> menuMap = r.getMenu().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getPrice()));
        resp.setMenu(menuMap);
        return resp;
    }
}
