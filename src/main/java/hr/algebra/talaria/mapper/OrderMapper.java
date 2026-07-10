package hr.algebra.talaria.mapper;

import hr.algebra.talaria.dto.OrderDto;
import hr.algebra.talaria.dto.OrderItemDto;
import hr.algebra.talaria.model.Order;

import java.util.List;

public class OrderMapper {

    private OrderMapper() {}

    public static OrderDto toDto(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase()
                ))
                .toList();

        return new OrderDto(
                order.getId(),
                order.getUser().getUsername(),
                order.getTotalPrice(),
                order.getPaymentMethod().name(),
                order.getStatus().name(),
                order.getCreatedAt(),
                items
        );
    }
}
