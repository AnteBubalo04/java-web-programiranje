package hr.algebra.ledvision.dto;

import hr.algebra.ledvision.model.AdExample;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdExampleDto {
    private Long id;
    private Long packageId;
    private String mediaUrl;
    private AdExample.MediaType mediaType;
    private String caption;
}