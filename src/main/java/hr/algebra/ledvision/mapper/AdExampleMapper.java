package hr.algebra.ledvision.mapper;

import hr.algebra.ledvision.dto.AdExampleDto;
import hr.algebra.ledvision.model.AdExample;

public class AdExampleMapper {

    private AdExampleMapper() {}

    public static AdExampleDto toDto(AdExample example) {
        return new AdExampleDto(
                example.getId(),
                example.getAdSpacePackage() != null ? example.getAdSpacePackage().getId() : null,
                example.getMediaUrl(),
                example.getMediaType(),
                example.getCaption()
        );
    }
}