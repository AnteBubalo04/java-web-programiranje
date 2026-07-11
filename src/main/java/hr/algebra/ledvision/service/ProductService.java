package hr.algebra.ledvision.service;

import hr.algebra.ledvision.model.Location;
import hr.algebra.ledvision.model.Product;
import hr.algebra.ledvision.repository.LocationRepository;
import hr.algebra.ledvision.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Serves entities (not DTOs) straight to Thymeleaf templates - the MVC-side
// counterpart to LocationService/ProductApiController which serve JSON DTOs
// to the REST API. Also still owns Location CRUD for the admin screens, exactly
// like the original ProductService owned Category CRUD - not cleaned up here to
// keep this step a pure rename, see PLAN.md Phase 3 for the full Product ->
// AdSpacePackage pass where this class itself gets renamed/reworked.
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;

    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrueWithCategory();
    }

    public List<Product> getProductsByLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .map(productRepository::findByCategoryAndActiveTrueWithCategory)
                .orElse(List.of());
    }

    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrueWithCategory(name);
    }
    public Optional<Product> getProductById(Long id) {
         return productRepository.findByIdWithCategory(id);
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
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

    public Optional<Location> getLocationById(Long id) {
        return locationRepository.findById(id);
    }

    public void saveLocation(Location location) {
        locationRepository.save(location);
    }

    public void deleteLocation(Long id) {
        locationRepository.deleteById(id);
    }



}