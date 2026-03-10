package com.foodorder.controller;

import com.foodorder.domain.Order;
import com.foodorder.dto.OrderDTOs;
import com.foodorder.enums.OrderStatus;
import com.foodorder.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** POST /api/orders — Place a new order */
    @PostMapping
    public ResponseEntity<OrderDTOs.OrderResponse> placeOrder(
            @Valid @RequestBody OrderDTOs.PlaceOrderRequest request) {

        Order order = orderService.placeOrder(
                request.getCustomerName(),
                request.getItems(),
                request.getSelectionStrategy());

        OrderDTOs.OrderResponse response = toResponse(order);
        response.setMessage("Order assigned to " + order.getAssignedRestaurantName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** GET /api/orders — List all orders */
    @GetMapping
    public ResponseEntity<List<OrderDTOs.OrderResponse>> listAll(
            @RequestParam(required = false) OrderStatus status) {

        List<Order> orders = (status != null)
                ? orderService.findByStatus(status)
                : orderService.findAll();

        return ResponseEntity.ok(orders.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    /** GET /api/orders/{orderId} — Get order by ID */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTOs.OrderResponse> getById(@PathVariable String orderId) {
        return ResponseEntity.ok(toResponse(orderService.findByIdOrThrow(orderId)));
    }

    /** PATCH /api/orders/{orderId}/complete — Mark order as completed */
    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<OrderDTOs.OrderResponse> completeOrder(
            @PathVariable String orderId,
            @RequestParam String restaurantName) {

        Order order = orderService.completeOrder(orderId, restaurantName);
        OrderDTOs.OrderResponse response = toResponse(order);
        response.setMessage("Order marked as COMPLETED. Restaurant capacity freed.");
        return ResponseEntity.ok(response);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private OrderDTOs.OrderResponse toResponse(Order o) {
        OrderDTOs.OrderResponse resp = new OrderDTOs.OrderResponse();
        resp.setOrderId(o.getOrderId());
        resp.setCustomerName(o.getCustomerName());
        resp.setStatus(o.getStatus());
        resp.setAssignedRestaurantName(o.getAssignedRestaurantName());
        resp.setTotalBill(o.getTotalBill());
        resp.setPlacedAt(o.getPlacedAt());
        resp.setItems(o.getItemsAsMap());
        return resp;
    }
}
