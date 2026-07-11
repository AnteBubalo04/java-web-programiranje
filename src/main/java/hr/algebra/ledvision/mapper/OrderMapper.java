package hr.algebra.ledvision.mapper;

import hr.algebra.ledvision.dto.OrderDto;
import hr.algebra.ledvision.dto.OrderItemDto;
import hr.algebra.ledvision.model.Order;
import hr.algebra.ledvision.model.PricingTier;

import java.util.List;

public class OrderMapper {

    private OrderMapper() {}

    public static OrderDto toDto(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(item -> {
                    PricingTier tier = item.getPricingTier();
                    return new OrderItemDto(
                            tier.getId(),
                            tier.getAdSpacePackage().getName(),
                            tier.getDurationLabel() + " / " + tier.getSizeLabel(),
                            item.getQuantity(),
                            item.getPriceAtPurchase()
                    );
                })
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