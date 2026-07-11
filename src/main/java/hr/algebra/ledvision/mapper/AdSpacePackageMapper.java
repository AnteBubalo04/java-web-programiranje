package hr.algebra.ledvision.mapper;

import hr.algebra.ledvision.dto.AdSpacePackageDto;
import hr.algebra.ledvision.model.AdSpacePackage;


public class AdSpacePackageMapper {

    private AdSpacePackageMapper() {}

    public static AdSpacePackageDto toDto(AdSpacePackage p) {
        return new AdSpacePackageDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStockQuantity(),
                p.getImageUrl(),
                p.getLocation() != null ? p.getLocation().getId() : null,
                p.getLocation() != null ? p.getLocation().getName() : null
        );
    }

    public static AdSpacePackage toEntity(AdSpacePackageDto dto) {
        AdSpacePackage p = new AdSpacePackage();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStockQuantity(dto.getStockQuantity());
        p.setImageUrl(dto.getImageUrl());
        return p;
    }
}