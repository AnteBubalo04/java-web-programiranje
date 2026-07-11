package hr.algebra.ledvision.repository;

import hr.algebra.ledvision.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);
    List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.items i "+
            "LEFT JOIN FETCH i.product p "+
            "LEFT JOIN FETCH p.category WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.product p " +
            "LEFT JOIN FETCH p.category " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithItemsOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product p LEFT JOIN FETCH p.category ORDER BY o.createdAt DESC")
    List<Order> findAllWithUserOrderByCreatedAtDesc();
}