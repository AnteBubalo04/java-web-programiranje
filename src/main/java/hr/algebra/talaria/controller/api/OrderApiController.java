package hr.algebra.talaria.controller.api;

import hr.algebra.talaria.dto.OrderDto;
import hr.algebra.talaria.mapper.OrderMapper;
import hr.algebra.talaria.model.Order;
import hr.algebra.talaria.security.CustomUserDetails;
import hr.algebra.talaria.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static hr.algebra.talaria.mapper.OrderMapper.toDto;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API (requires JWT)")
@SecurityRequirement(name = "bearerAuth")
public class OrderApiController {

    private final OrderService orderService;

    @GetMapping("/my")
    @Operation(summary = "Get my orders (authenticated user)")
    public List<OrderDto> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return orderService.getOrdersByUserId(userId).stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID (authenticated user)")
    public ResponseEntity<OrderDto> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return orderService.getOrderById(id)
                .filter(order -> order.getUser().getId()
                        .equals(userDetails.getUser().getId()))
                .map(order -> ResponseEntity.ok(toDto(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders (ADMIN only)")
    public List<OrderDto> getAllOrders() {
        return orderService.getAllOrders().stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (ADMIN only)")
    public ResponseEntity<OrderDto> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        orderService.updateOrderStatus(id, Order.OrderStatus.valueOf(status));
        return orderService.getOrderById(id)
                .map(order -> ResponseEntity.ok(toDto(order)))
                .orElse(ResponseEntity.notFound().build());

    }


}