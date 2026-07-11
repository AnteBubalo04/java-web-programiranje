package hr.algebra.ledvision.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

// A priced booking option within an AdSpacePackage (e.g. "1 week / small screen /
// 149 EUR"). This is the new piece that didn't exist under the old Product model,
// where a single price lived directly on the product - here, duration and screen
// size both affect price, so a package can offer several tiers to choose from.
// A cart/order item now points at a specific tier, not at the package directly.
@Entity
@Table(name = "pricing_tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private AdSpacePackage adSpacePackage;

    @Column(nullable = false, length = 50)
    private String durationLabel;

    @Column(nullable = false, length = 50)
    private String sizeLabel;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}