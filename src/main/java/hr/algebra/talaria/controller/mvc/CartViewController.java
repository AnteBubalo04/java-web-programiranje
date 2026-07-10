package hr.algebra.talaria.controller.mvc;

import hr.algebra.talaria.model.Product;
import hr.algebra.talaria.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartViewController {

    private final CartService cartService;

    @GetMapping
    public String cart(HttpSession session, Model model) {
        Map<Long, Integer> cart = getOrCreateCart(session);
        Map<Product, Integer> cartItems = cartService.getCartItems(cart);

        model.addAttribute("cartItems", cartService.getCartItems(cart));
        model.addAttribute("cartTotal", cartService.getCartTotal(cart));
        model.addAttribute("cartCount", cartService.getCartItemCount(cart));

        Map<Long, BigDecimal> itemTotals = new HashMap<>();
        cartItems.forEach((product, quantity) ->
                itemTotals.put(product.getId(),
                        cartService.getItemTotal(product, quantity))
        );
        model.addAttribute("itemTotals", itemTotals);

        return "cart/cart";
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getOrCreateCart(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }
        return cart;
    }
}