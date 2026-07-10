package hr.algebra.talaria.service;

import hr.algebra.talaria.model.Product;
import hr.algebra.talaria.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;

    public void addToCart(Map<Long, Integer> cart, Long productId, int quantity) {
        productRepository.findById(productId).ifPresent(product -> {
            if (product.isActive() && product.getStockQuantity() >= quantity) {
                cart.merge(productId, quantity, Integer::sum);
            }
        });
    }

    public void removeFromCart(Map<Long, Integer> cart, Long productId) {
        cart.remove(productId);
    }

    public void updateQuantity(Map<Long, Integer> cart, Long productId, int quantity) {
        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }
    }

    public void clearCart(Map<Long, Integer> cart) {
        cart.clear();
    }


    public Map<Product, Integer> getCartItems(Map<Long, Integer> cart) {
        if (cart.isEmpty()) return Collections.emptyMap();

        Map<Product, Integer> items = new HashMap<>();
        cart.forEach((productId, quantity) ->
                productRepository.findByIdWithCategory(productId)
                        .ifPresent(product -> items.put(product, quantity))
        );
        return items;
    }

    public BigDecimal getItemTotal(Product product, Integer quantity) {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getCartTotal(Map<Long, Integer> cart) {
        return getCartItems(cart).entrySet().stream()
                .map(entry -> entry.getKey().getPrice()
                        .multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getCartItemCount(Map<Long, Integer> cart) {
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }
}