package hr.algebra.ledvision.repository;

import hr.algebra.ledvision.model.AdExample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdExampleRepository extends JpaRepository<AdExample, Long> {
    List<AdExample> findByAdSpacePackageId(Long packageId);
}