package hr.algebra.ledvision.controller.api;

import hr.algebra.ledvision.dto.CartAddRequest;
import hr.algebra.ledvision.dto.CartUpdateRequest;
import hr.algebra.ledvision.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @RequestBody CartAddRequest request,
            HttpSession session) {

        Map<Long, Integer> cart = getOrCreateCart(session);
        cartService.addToCart(cart, request.tierId(), request.quantity());
        session.setAttribute("cart", cart);

        return ResponseEntity.ok("Added to cart!");
    }

    @DeleteMapping("/remove/{tierId}")
    public ResponseEntity<String> removeFromCart(
            @PathVariable Long tierId,
            HttpSession session) {

        Map<Long, Integer> cart = getOrCreateCart(session);
        cartService.removeFromCart(cart, tierId);
        session.setAttribute("cart", cart);

        return ResponseEntity.ok("Removed from cart!");
    }

    @PutMapping("/update/{tierId}")
    public ResponseEntity<String> updateQuantity(
            @PathVariable Long tierId,
            @RequestBody CartUpdateRequest request,
            HttpSession session) {

        Map<Long, Integer> cart = getOrCreateCart(session);
        cartService.updateQuantity(cart, tierId, request.quantity());
        session.setAttribute("cart", cart);

        return ResponseEntity.ok("Quantity updated!");
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(HttpSession session) {
        Map<Long, Integer> cart = getOrCreateCart(session);
        cartService.clearCart(cart);
        session.setAttribute("cart", cart);

        return ResponseEntity.ok("Cart has been cleared!");
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartCount(HttpSession session) {
        Map<Long, Integer> cart = getOrCreateCart(session);
        return ResponseEntity.ok(cartService.getCartItemCount(cart));
    }

    private Map<Long, Integer> getOrCreateCart(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }
        return cart;
    }
}