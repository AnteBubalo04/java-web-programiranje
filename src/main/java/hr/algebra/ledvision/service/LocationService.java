package hr.algebra.ledvision.service;

import hr.algebra.ledvision.dto.LocationDto;
import hr.algebra.ledvision.mapper.LocationMapper;
import hr.algebra.ledvision.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// DTO-facing counterpart to LocationRepository, used by the REST API layer
// (LocationApiController). The MVC/Thymeleaf side reads Location entities
// directly through AdSpacePackageService instead of going through DTOs here -
// same split that already existed between CategoryService and ProductService.
@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

  public List<LocationDto> getAllLocations() {
        return locationRepository.findAll().stream().map(LocationMapper::toDto).toList();
    }

    public Optional<LocationDto> getLocationById(Long id) {
      return locationRepository.findById(id).map(LocationMapper::toDto);
    }
}