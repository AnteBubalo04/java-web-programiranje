package hr.algebra.ledvision.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdSpacePackageDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Long locationId;
    private String locationName;
}