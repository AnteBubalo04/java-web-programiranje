package hr.algebra.ledvision.repository;

import hr.algebra.ledvision.model.Product;
import hr.algebra.ledvision.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

// NOTE: "category"/"Category" in the JPQL and method names below refers to the
// Product.category field (still named that way, see Product.java) - it is a
// Location under the hood now. Renamed together with Product -> AdSpacePackage
// in the next refactor step.
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true")
    List<Product> findByActiveTrueWithCategory();

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true AND p.category = :category")
    List<Product> findByCategoryAndActiveTrueWithCategory(@Param("category") Location category);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCaseAndActiveTrueWithCategory(@Param("name") String name);
}