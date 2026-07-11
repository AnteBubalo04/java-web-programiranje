package hr.algebra.ledvision.mapper;

import hr.algebra.ledvision.dto.LocationDto;
import hr.algebra.ledvision.model.Location;


public class LocationMapper {

   private LocationMapper(){}

    public static LocationDto toDto(Location location) {
        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setDescription(location.getDescription());
        dto.setImageUrl(location.getImageUrl());
        return dto;
    }

    public static Location toEntity(LocationDto dto) {
        Location l = new Location();
        l.setName(dto.getName());
        l.setDescription(dto.getDescription());
        l.setImageUrl(dto.getImageUrl());
        return l;
    }

}