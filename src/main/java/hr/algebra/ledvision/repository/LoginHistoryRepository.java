package hr.algebra.ledvision.repository;

import hr.algebra.ledvision.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    @Query("SELECT l FROM LoginHistory l JOIN FETCH l.user ORDER BY l.loggedAt DESC")
    List<LoginHistory> findAllWithUserOrderByLoggedAtDesc();
}