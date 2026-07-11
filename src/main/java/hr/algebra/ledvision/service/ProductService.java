package hr.algebra.ledvision.service;

import hr.algebra.ledvision.model.Category;
import hr.algebra.ledvision.model.Product;
import hr.algebra.ledvision.repository.CategoryRepository;
import hr.algebra.ledvision.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrueWithCategory();
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(productRepository::findByCategoryAndActiveTrueWithCategory)
                .orElse(List.of());
    }

    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrueWithCategory(name);
    }
    public Optional<Product> getProductById(Long id) {
         return productRepository.findByIdWithCategory(id);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }



    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            product.setActive(false);
            productRepository.save(product);
        });
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }



}