package hr.algebra.ledvision.repository;

import hr.algebra.ledvision.model.Product;
import hr.algebra.ledvision.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true")
    List<Product> findByActiveTrueWithCategory();

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true AND p.category = :category")
    List<Product> findByCategoryAndActiveTrueWithCategory(@Param("category") Category category);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCaseAndActiveTrueWithCategory(@Param("name") String name);
}