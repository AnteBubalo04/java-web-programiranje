package hr.algebra.talaria.controller.mvc;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import hr.algebra.talaria.model.Order;
import hr.algebra.talaria.security.CustomUserDetails;
import hr.algebra.talaria.service.CartService;
import hr.algebra.talaria.service.OrderService;
import hr.algebra.talaria.service.PayPalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderViewController {

    private final OrderService orderService;
    private final CartService cartService;
    private final PayPalService payPalService;



    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Map<Long, Integer> cart = getOrCreateCart(session);

        if (cart.isEmpty()) {
            return "redirect:/cart";
        }

        Map<Long, java.math.BigDecimal> itemTotals = new HashMap<>();
        cartService.getCartItems(cart).forEach((product, quantity) ->
                itemTotals.put(product.getId(),
                        cartService.getItemTotal(product, quantity))
        );

        model.addAttribute("cartItems", cartService.getCartItems(cart));
        model.addAttribute("cartTotal", cartService.getCartTotal(cart));
        model.addAttribute("itemTotals", itemTotals);
        return "orders/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(
            @RequestParam String paymentMethod,
            @RequestParam String fullName,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String postalCode,
            @RequestParam String country,
            HttpSession session,
            Authentication authentication,
            HttpServletRequest request) throws PayPalRESTException {

        Map<Long, Integer> cart = getOrCreateCart(session);
        if (cart.isEmpty()) return "redirect:/cart";

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();


        if ("PAYPAL".equals(paymentMethod)) {
            BigDecimal total = cartService.getCartTotal(cart);
            String baseUrl = request.getScheme() + "://" +
                    request.getServerName() + ":" +
                    request.getServerPort();

            Payment payment = payPalService.createPayment(
                    total,
                    "EUR",
                    baseUrl + "/checkout/paypal/success",
                    baseUrl + "/checkout/paypal/cancel"
            );


            session.setAttribute("paypalCart", cart);
            session.setAttribute("paypalUserId", userId);
            session.setAttribute("shipFullName", fullName);
            session.setAttribute("shipAddress", address);
            session.setAttribute("shipCity", city);
            session.setAttribute("shipPostalCode", postalCode);
            session.setAttribute("shipCountry", country);

            for (Links link : payment.getLinks()) {
                if ("approval_url".equals(link.getRel())) {
                    return "redirect:" + link.getHref();
                }
            }
        }


        Order.PaymentMethod method = Order.PaymentMethod.valueOf(paymentMethod);
        Order order = orderService.createOrder(userId, cart, method,
                fullName, address, city, postalCode, country);
        cartService.clearCart(cart);
        session.setAttribute("cart", cart);

        return "redirect:/orders/" + order.getId();
    }

    @GetMapping("/checkout/paypal/success")
    public String paypalSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            HttpSession session) throws PayPalRESTException {

        Payment payment = payPalService.executePayment(paymentId, payerId);

        if ("approved".equals(payment.getState())) {
            Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("paypalCart");
                Long userId = (Long) session.getAttribute("paypalUserId");

            Order order = orderService.createOrder(
                    userId, cart, Order.PaymentMethod.PAYPAL,
                    (String) session.getAttribute("shipFullName"),
                    (String) session.getAttribute("shipAddress"),
                    (String) session.getAttribute("shipCity"),
                    (String) session.getAttribute("shipPostalCode"),
                    (String) session.getAttribute("shipCountry"));

            cartService.clearCart(cart);
            session.removeAttribute("paypalCart");
            session.removeAttribute("paypalUserId");
            session.removeAttribute("shipFullName");
            session.removeAttribute("shipAddress");
            session.removeAttribute("shipCity");
            session.removeAttribute("shipPostalCode");
            session.removeAttribute("shipCountry");
            session.setAttribute("cart", new HashMap<>());

            return "redirect:/orders/" + order.getId();
        }

        return "redirect:/checkout?error=paypal";
    }

    @GetMapping("/checkout/paypal/cancel")
    public String paypalCancel() {
        return "redirect:/cart";
    }


    @GetMapping("/orders")
    public String orderHistory(Authentication authentication, Model model) {
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();

        model.addAttribute("orders", orderService.getOrdersByUserId(userId));
        return "orders/history";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id,
                              Authentication authentication,
                              Model model) {
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();

        orderService.getOrdersByUserId(userId).stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .ifPresent(order -> {
                    model.addAttribute("order", order);

                    Map<Long, BigDecimal> itemTotals = new HashMap<>();
                    order.getItems().forEach(item ->
                            itemTotals.put(item.getId(),
                                    orderService.getOrderItemTotal(item))
                    );
                    model.addAttribute("itemTotals", itemTotals);
                });

        return "orders/detail";
    }


    private Map<Long, Integer> getOrCreateCart(HttpSession session) {
        Map<Long, Integer> cart =
                (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }
        return cart;
    }
}