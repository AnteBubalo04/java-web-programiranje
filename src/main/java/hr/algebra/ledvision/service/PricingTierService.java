package hr.algebra.ledvision.service;

import hr.algebra.ledvision.model.PricingTier;
import hr.algebra.ledvision.repository.AdSpacePackageRepository;
import hr.algebra.ledvision.repository.PricingTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// CRUD for PricingTier, kept as its own small service (mirrors how
// LocationService/AdSpacePackageService are each scoped to one entity)
// rather than folded into AdSpacePackageService, to keep classes short.
@Service
@RequiredArgsConstructor
public class PricingTierService {

    private final PricingTierRepository tierRepository;
    private final AdSpacePackageRepository packageRepository;

    public List<PricingTier> getTiersByPackageId(Long packageId) {
        return tierRepository.findByAdSpacePackageId(packageId);
    }

    public Optional<PricingTier> getTierById(Long id) {
        return tierRepository.findById(id);
    }

    public void saveTier(Long packageId, PricingTier tier) {
        packageRepository.findById(packageId).ifPresent(tier::setAdSpacePackage);
        tierRepository.save(tier);
    }

    public void deleteTier(Long id) {
        tierRepository.deleteById(id);
    }
}