package hr.algebra.ledvision.controller.mvc;

import hr.algebra.ledvision.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ProductViewController {

    private final ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("products", productService.getAllActiveProducts());
        return "index";
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            Model model) {

        if (search != null && !search.isEmpty()) {
            model.addAttribute("products", productService.searchProducts(search));
        } else if (categoryId != null) {
            model.addAttribute("products", productService.getProductsByCategory(categoryId));
        } else {
            model.addAttribute("products", productService.getAllActiveProducts());
        }

        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("search", search);
        return "products/list";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        productService.getProductById(id)
                .ifPresent(product -> model.addAttribute("product", product));
        return "products/detail";
    }
}