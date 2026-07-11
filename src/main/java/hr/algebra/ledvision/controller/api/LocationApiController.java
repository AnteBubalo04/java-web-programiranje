package hr.algebra.ledvision.controller.api;

import hr.algebra.ledvision.dto.LocationDto;
import hr.algebra.ledvision.model.Location;
import hr.algebra.ledvision.repository.LocationRepository;
import hr.algebra.ledvision.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static hr.algebra.ledvision.mapper.LocationMapper.toDto;
import static hr.algebra.ledvision.mapper.LocationMapper.toEntity;


@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Zagreb LED-screen location management API")
public class LocationApiController {

    private final LocationService locationService;
    private final LocationRepository locationRepository;

    @GetMapping
    @Operation(summary = "Get all Locations")
    public List<LocationDto> getAllLocations() {
        return locationService.getAllLocations();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Location by ID")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable Long id) {
        return locationService.getLocationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Location (ADMIN only)")
    public ResponseEntity<LocationDto> updateLocation(@PathVariable Long id,
                                                      @RequestBody LocationDto locationDto)
    {
        return locationRepository.findById(id)
                .map(l -> {
                    l.setName(locationDto.getName());
                    l.setDescription(locationDto.getDescription());
                    l.setImageUrl(locationDto.getImageUrl());
                    Location saved = locationRepository.save(l);
                    return ResponseEntity.ok(toDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Location (ADMIN only)")
    public ResponseEntity<LocationDto> create(@RequestBody LocationDto dto) {
        Location location = toEntity(dto);
        Location saved = locationRepository.save(location);
        return ResponseEntity.ok(toDto(saved));
    }



}