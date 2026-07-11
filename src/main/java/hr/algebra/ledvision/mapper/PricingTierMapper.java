package hr.algebra.ledvision.mapper;

import hr.algebra.ledvision.dto.PricingTierDto;
import hr.algebra.ledvision.model.PricingTier;

public class PricingTierMapper {

    private PricingTierMapper() {}

    public static PricingTierDto toDto(PricingTier tier) {
        return new PricingTierDto(
                tier.getId(),
                tier.getAdSpacePackage() != null ? tier.getAdSpacePackage().getId() : null,
                tier.getDurationLabel(),
                tier.getSizeLabel(),
                tier.getPrice()
        );
    }
}