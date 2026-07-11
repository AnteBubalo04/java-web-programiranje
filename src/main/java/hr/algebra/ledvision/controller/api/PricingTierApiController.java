package hr.algebra.ledvision.controller.api;

import hr.algebra.ledvision.dto.PricingTierDto;
import hr.algebra.ledvision.mapper.PricingTierMapper;
import hr.algebra.ledvision.model.PricingTier;
import hr.algebra.ledvision.service.PricingTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tiers")
@RequiredArgsConstructor
@Tag(name = "Pricing Tiers", description = "Pricing-tier management API")
public class PricingTierApiController {

    private final PricingTierService tierService;

    @GetMapping("/package/{packageId}")
    @Operation(summary = "Get pricing tiers for a package")
    public List<PricingTierDto> getByPackage(@PathVariable Long packageId) {
        return tierService.getTiersByPackageId(packageId).stream()
                .map(PricingTierMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pricing tier by ID")
    public ResponseEntity<PricingTierDto> getTier(@PathVariable Long id) {
        return tierService.getTierById(id)
                .map(t -> ResponseEntity.ok(PricingTierMapper.toDto(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create pricing tier (ADMIN only)")
    public ResponseEntity<PricingTierDto> create(@RequestBody PricingTierDto dto) {
        PricingTier tier = new PricingTier();
        tier.setDurationLabel(dto.getDurationLabel());
        tier.setSizeLabel(dto.getSizeLabel());
        tier.setPrice(dto.getPrice());
        tierService.saveTier(dto.getPackageId(), tier);
        return ResponseEntity.ok(PricingTierMapper.toDto(tier));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete pricing tier (ADMIN only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tierService.deleteTier(id);
        return ResponseEntity.noContent().build();
    }
}