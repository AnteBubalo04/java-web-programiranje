package hr.algebra.ledvision.service;

import hr.algebra.ledvision.model.AdSpacePackage;
import hr.algebra.ledvision.repository.AdSpacePackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// The session cart is still a plain Map<packageId, quantity>, same shape as the
// original Map<productId, quantity>. This gets reworked to key by PricingTier
// instead of package once pricing tiers exist (see PLAN.md Phase 4.3) - for now,
// "quantity" limited by stockQuantity mirrors the original stock-check behaviour.
@Service
@RequiredArgsConstructor
public class CartService {

    private final AdSpacePackageRepository packageRepository;

    public void addToCart(Map<Long, Integer> cart, Long packageId, int quantity) {
        packageRepository.findById(packageId).ifPresent(adSpacePackage -> {
            if (adSpacePackage.isActive() && adSpacePackage.getStockQuantity() >= quantity) {
                cart.merge(packageId, quantity, Integer::sum);
            }
        });
    }

    public void removeFromCart(Map<Long, Integer> cart, Long packageId) {
        cart.remove(packageId);
    }

    public void updateQuantity(Map<Long, Integer> cart, Long packageId, int quantity) {
        if (quantity <= 0) {
            cart.remove(packageId);
        } else {
            cart.put(packageId, quantity);
        }
    }

    public void clearCart(Map<Long, Integer> cart) {
        cart.clear();
    }


    public Map<AdSpacePackage, Integer> getCartItems(Map<Long, Integer> cart) {
        if (cart.isEmpty()) return Collections.emptyMap();

        Map<AdSpacePackage, Integer> items = new HashMap<>();
        cart.forEach((packageId, quantity) ->
                packageRepository.findByIdWithLocation(packageId)
                        .ifPresent(adSpacePackage -> items.put(adSpacePackage, quantity))
        );
        return items;
    }

    public BigDecimal getItemTotal(AdSpacePackage adSpacePackage, Integer quantity) {
        return adSpacePackage.getPrice().multiply(BigDecimal.valueOf(quantity));
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