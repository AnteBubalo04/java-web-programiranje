package hr.algebra.ledvision.controller.api;

import hr.algebra.ledvision.dto.AdSpacePackageDto;
import hr.algebra.ledvision.mapper.AdSpacePackageMapper;
import hr.algebra.ledvision.model.AdSpacePackage;
import hr.algebra.ledvision.service.AdSpacePackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static hr.algebra.ledvision.mapper.AdSpacePackageMapper.toDto;
import static hr.algebra.ledvision.mapper.AdSpacePackageMapper.toEntity;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@Tag(name = "Packages", description = "Ad-space package management API")
public class AdSpacePackageApiController {

    private final AdSpacePackageService packageService;

    @GetMapping
    @Operation(summary = "Get all active packages")
    public List<AdSpacePackageDto> getAllPackages() {
        return packageService.getAllActivePackages().stream()
                .map(AdSpacePackageMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get package by ID")
    public ResponseEntity<AdSpacePackageDto> getPackage(@PathVariable Long id) {
        return packageService.getPackageById(id)
                .map(p -> ResponseEntity.ok(toDto(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Get packages by location")
    public List<AdSpacePackageDto> getByLocation(@PathVariable Long locationId) {
        return packageService.getPackagesByLocation(locationId).stream()
                .map(AdSpacePackageMapper::toDto)
                .toList();
    }

    @GetMapping("/search")
    @Operation(summary = "Search packages by name")
    public List<AdSpacePackageDto> search(@RequestParam String name) {
        return packageService.searchPackages(name).stream()
                .map(AdSpacePackageMapper::toDto)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create package (ADMIN only)")
    public ResponseEntity<AdSpacePackageDto> create(@RequestBody AdSpacePackageDto dto) {
        AdSpacePackage adSpacePackage = toEntity(dto);
        packageService.getLocationById(dto.getLocationId())
                .ifPresent(adSpacePackage::setLocation);
        AdSpacePackage saved = packageService.savePackage(adSpacePackage);
        return ResponseEntity.ok(toDto(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update package (ADMIN only)")
    public ResponseEntity<AdSpacePackageDto> update(@PathVariable Long id,
                                             @RequestBody AdSpacePackageDto dto) {
        return packageService.getPackageById(id)
                .map(adSpacePackage -> {
                    adSpacePackage.setName(dto.getName());
                    adSpacePackage.setDescription(dto.getDescription());
                    adSpacePackage.setImageUrl(dto.getImageUrl());
                    packageService.getLocationById(dto.getLocationId())
                            .ifPresent(adSpacePackage::setLocation);
                    return ResponseEntity.ok(toDto(packageService.savePackage(adSpacePackage)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete package (ADMIN only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }

}