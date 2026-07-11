package hr.algebra.ledvision.service;

import hr.algebra.ledvision.model.AdSpacePackage;
import hr.algebra.ledvision.model.Location;
import hr.algebra.ledvision.repository.AdSpacePackageRepository;
import hr.algebra.ledvision.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Serves entities (not DTOs) straight to Thymeleaf templates - the MVC-side
// counterpart to LocationService/AdSpacePackageApiController which serve JSON
// DTOs to the REST API. Also still owns Location CRUD for the admin screens
// (unchanged since the Category -> Location rename step) - see PLAN.md.
@Service
@RequiredArgsConstructor
public class AdSpacePackageService {

    private final AdSpacePackageRepository packageRepository;
    private final LocationRepository locationRepository;

    public List<AdSpacePackage> getAllActivePackages() {
        return packageRepository.findByActiveTrueWithLocation();
    }

    public List<AdSpacePackage> getPackagesByLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .map(packageRepository::findByLocationAndActiveTrueWithLocation)
                .orElse(List.of());
    }

    public List<AdSpacePackage> searchPackages(String name) {
        return packageRepository.findByNameContainingIgnoreCaseAndActiveTrueWithLocation(name);
    }
    public Optional<AdSpacePackage> getPackageById(Long id) {
         return packageRepository.findByIdWithLocation(id);
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }



    public AdSpacePackage savePackage(AdSpacePackage adSpacePackage) {
        return packageRepository.save(adSpacePackage);
    }

    public void deletePackage(Long id) {
        packageRepository.findById(id).ifPresent(adSpacePackage -> {
            adSpacePackage.setActive(false);
            packageRepository.save(adSpacePackage);
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