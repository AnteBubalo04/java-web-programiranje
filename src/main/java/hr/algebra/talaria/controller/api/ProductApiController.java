package hr.algebra.talaria.controller.api;

import hr.algebra.talaria.dto.ProductDto;
import hr.algebra.talaria.mapper.ProductMapper;
import hr.algebra.talaria.model.Product;
import hr.algebra.talaria.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static hr.algebra.talaria.mapper.ProductMapper.toDto;
import static hr.algebra.talaria.mapper.ProductMapper.toEntity;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management API")
public class ProductApiController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all active products")
    public List<ProductDto> getAllProducts() {
        return productService.getAllActiveProducts().stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(p -> ResponseEntity.ok(toDto(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public List<ProductDto> getByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategory(categoryId).stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name")
    public List<ProductDto> search(@RequestParam String name) {
        return productService.searchProducts(name).stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product (ADMIN only)")
    public ResponseEntity<ProductDto> create(@RequestBody ProductDto dto) {
        Product product = toEntity(dto);
        productService.getCategoryById(dto.getCategoryId())
                .ifPresent(product::setCategory);
        Product saved = productService.saveProduct(product);
        return ResponseEntity.ok(toDto(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product (ADMIN only)")
    public ResponseEntity<ProductDto> update(@PathVariable Long id,
                                             @RequestBody ProductDto dto) {
        return productService.getProductById(id)
                .map(product -> {
                    product.setName(dto.getName());
                    product.setDescription(dto.getDescription());
                    product.setPrice(dto.getPrice());
                    product.setStockQuantity(dto.getStockQuantity());
                    product.setImageUrl(dto.getImageUrl());
                    productService.getCategoryById(dto.getCategoryId())
                            .ifPresent(product::setCategory);
                    return ResponseEntity.ok(toDto(productService.saveProduct(product)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product (ADMIN only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

}