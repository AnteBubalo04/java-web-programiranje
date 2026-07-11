package hr.algebra.ledvision.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingTierDto {
    private Long id;
    private Long packageId;
    private String durationLabel;
    private String sizeLabel;
    private BigDecimal price;
}