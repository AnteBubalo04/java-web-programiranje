package hr.algebra.ledvision.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String username;
    private BigDecimal totalPrice;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items;
}