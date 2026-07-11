package hr.algebra.ledvision.repository;

import hr.algebra.ledvision.model.PricingTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingTierRepository extends JpaRepository<PricingTier, Long> {

    List<PricingTier> findByAdSpacePackageId(Long packageId);

    @Query("SELECT t FROM PricingTier t JOIN FETCH t.adSpacePackage p JOIN FETCH p.location WHERE t.id = :id")
    Optional<PricingTier> findByIdWithPackageAndLocation(@Param("id") Long id);
}