package hr.algebra.ledvision.repository;

import hr.algebra.ledvision.model.AdSpacePackage;
import hr.algebra.ledvision.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdSpacePackageRepository extends JpaRepository<AdSpacePackage, Long> {

    @Query("SELECT p FROM AdSpacePackage p JOIN FETCH p.location WHERE p.id = :id")
    Optional<AdSpacePackage> findByIdWithLocation(@Param("id") Long id);

    @Query("SELECT p FROM AdSpacePackage p JOIN FETCH p.location WHERE p.active = true")
    List<AdSpacePackage> findByActiveTrueWithLocation();

    @Query("SELECT p FROM AdSpacePackage p JOIN FETCH p.location WHERE p.active = true AND p.location = :location")
    List<AdSpacePackage> findByLocationAndActiveTrueWithLocation(@Param("location") Location location);

    @Query("SELECT p FROM AdSpacePackage p JOIN FETCH p.location WHERE p.active = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<AdSpacePackage> findByNameContainingIgnoreCaseAndActiveTrueWithLocation(@Param("name") String name);
}