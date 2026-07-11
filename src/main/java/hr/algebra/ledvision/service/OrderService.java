package hr.algebra.ledvision.service;

import hr.algebra.ledvision.model.*;
import hr.algebra.ledvision.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    @Transactional
    public Order createOrder(Long userId,
                             Map<Long, Integer> cart,
                             Order.PaymentMethod paymentMethod,
                             String fullName, String address,
                             String city, String postalCode, String country) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty!");
        }

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setShippingFullName(fullName);
        order.setShippingAddress(address);
        order.setShippingCity(city);
        order.setShippingPostalCode(postalCode);
        order.setShippingCountry(country);

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found!"));

            if (product.getStockQuantity() < entry.getValue()) {
                throw new IllegalArgumentException(
                        "No stock available: " + product.getName());
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(entry.getValue());
            item.setPriceAtPurchase(product.getPrice());

            order.getItems().add(item);

            total = total.add(
                    product.getPrice().multiply(BigDecimal.valueOf(entry.getValue()))
            );

            product.setStockQuantity(product.getStockQuantity() - entry.getValue());
            productRepository.save(product);
        }

        order.setTotalPrice(total);
        return orderRepository.save(order);
    }



    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdWithItemsOrderByCreatedAtDesc(userId);
    }

    public BigDecimal getOrderItemTotal(OrderItem item) {
        return item.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findByIdWithItems(id);
    }


    public List<Order> getAllOrders() {
        return orderRepository.findAllWithUserOrderByCreatedAtDesc();
    }


    @Transactional
    public void updateOrderStatus(Long orderId, Order.OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);

            if (status == Order.OrderStatus.SHIPPED) {
                order.setShippedAt(LocalDateTime.now());
            }
            if (status == Order.OrderStatus.DELIVERED) {
                order.setDeliveredAt(LocalDateTime.now());
            }

            orderRepository.save(order);
        });
    }

    @Transactional
    public void updateTrackingNumber(Long orderId, String trackingNumber) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setTrackingNumber(trackingNumber);
            orderRepository.save(order);
        });
    }
}