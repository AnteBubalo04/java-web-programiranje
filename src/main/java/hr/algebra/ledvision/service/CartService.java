package hr.algebra.ledvision.service;

import hr.algebra.ledvision.model.PricingTier;
import hr.algebra.ledvision.repository.PricingTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// The session cart is a plain Map<tierId, quantity> - "quantity" means how many
// units of that tier's duration/size are being reserved (e.g. 2x "1 month" tier
// = 2 consecutive months), not a physical stock count. There is deliberately no
// calendar/overlap availability check here - see PLAN.md for why that's out of
// scope for this refactor.
@Service
@RequiredArgsConstructor
public class CartService {

    private final PricingTierRepository tierRepository;

    public void addToCart(Map<Long, Integer> cart, Long tierId, int quantity) {
        tierRepository.findById(tierId).ifPresent(tier -> {
            if (quantity > 0) {
                cart.merge(tierId, quantity, Integer::sum);
            }
        });
    }

    public void removeFromCart(Map<Long, Integer> cart, Long tierId) {
        cart.remove(tierId);
    }

    public void updateQuantity(Map<Long, Integer> cart, Long tierId, int quantity) {
        if (quantity <= 0) {
            cart.remove(tierId);
        } else {
            cart.put(tierId, quantity);
        }
    }

    public void clearCart(Map<Long, Integer> cart) {
        cart.clear();
    }


    public Map<PricingTier, Integer> getCartItems(Map<Long, Integer> cart) {
        if (cart.isEmpty()) return Collections.emptyMap();

        Map<PricingTier, Integer> items = new HashMap<>();
        cart.forEach((tierId, quantity) ->
                tierRepository.findByIdWithPackageAndLocation(tierId)
                        .ifPresent(tier -> items.put(tier, quantity))
        );
        return items;
    }

    public BigDecimal getItemTotal(PricingTier tier, Integer quantity) {
        return tier.getPrice().multiply(BigDecimal.valueOf(quantity));
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