package hr.algebra.ledvision.service;

import hr.algebra.ledvision.model.AdExample;
import hr.algebra.ledvision.repository.AdExampleRepository;
import hr.algebra.ledvision.repository.AdSpacePackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// CRUD for AdExample, same small-service-per-entity shape as PricingTierService.
@Service
@RequiredArgsConstructor
public class AdExampleService {

    private final AdExampleRepository exampleRepository;
    private final AdSpacePackageRepository packageRepository;

    public List<AdExample> getExamplesByPackageId(Long packageId) {
        return exampleRepository.findByAdSpacePackageId(packageId);
    }

    public Optional<AdExample> getExampleById(Long id) {
        return exampleRepository.findById(id);
    }

    public void saveExample(Long packageId, AdExample example) {
        packageRepository.findById(packageId).ifPresent(example::setAdSpacePackage);
        exampleRepository.save(example);
    }

    public void deleteExample(Long id) {
        exampleRepository.deleteById(id);
    }
}