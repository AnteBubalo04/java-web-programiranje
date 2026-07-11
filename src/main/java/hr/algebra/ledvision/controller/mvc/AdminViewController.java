package hr.algebra.ledvision.controller.mvc;

import hr.algebra.ledvision.model.Category;
import hr.algebra.ledvision.model.Order;
import hr.algebra.ledvision.model.Product;
import hr.algebra.ledvision.repository.LoginHistoryRepository;
import hr.algebra.ledvision.service.OrderService;
import hr.algebra.ledvision.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {

    private final ProductService productService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final OrderService orderService;

    private static final String CATEGORY_VIEW = "category";
    private static final String PRODUCT_VIEW = "product";
    private static final String ORDERS_VIEW = "orders";
    private static final String CATEGORIES_VIEW = "categories";
    private static final String PRODUCTS_VIEW = "products";
    private static final String LOGIN_HISTORY_VIEW = "loginHistory";



    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute(PRODUCTS_VIEW, productService.getAllActiveProducts());
        model.addAttribute(CATEGORIES_VIEW, productService.getAllCategories());
        return "admin/dashboard";
    }



    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute(CATEGORY_VIEW, new Category());
        return "admin/category-form";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        productService.getCategoryById(id)
                .ifPresent(category -> model.addAttribute(CATEGORY_VIEW, category));
        return "admin/category-form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category) {
        productService.saveCategory(category);
        return "redirect:/admin";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        productService.deleteCategory(id);
        return "redirect:/admin";
    }



    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute(PRODUCT_VIEW, new Product());
        model.addAttribute(CATEGORIES_VIEW, productService.getAllCategories());
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        productService.getProductById(id)
                .ifPresent(product -> model.addAttribute(PRODUCT_VIEW, product));
        model.addAttribute(CATEGORIES_VIEW, productService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam java.math.BigDecimal price,
            @RequestParam Integer stockQuantity,
            @RequestParam(required = false) String imageUrl,
            @RequestParam Long categoryId) {

        Product product = (id != null)
                ? productService.getProductById(id).orElse(new Product())
                : new Product();

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setImageUrl(imageUrl);

        productService.getCategoryById(categoryId)
                .ifPresent(product::setCategory);

        productService.saveProduct(product);
        return "redirect:/admin";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin";
    }


    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute(ORDERS_VIEW, orderService.getAllOrders());
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status) {
        orderService.updateOrderStatus(id,
                Order.OrderStatus.valueOf(status));
        return "redirect:/admin/orders";
    }



    @GetMapping("/login-history")
    public String loginHistory(Model model) {
        model.addAttribute(LOGIN_HISTORY_VIEW,
                loginHistoryRepository.findAllWithUserOrderByLoggedAtDesc());
        return "admin/login-history";
    }
}