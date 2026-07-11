package hr.algebra.ledvision.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

// A bookable LED-advertising offer at a single Location (e.g. "Prime evening loop -
// Ban Jelačić Square"). This used to be "Product" (a physical motorcycle part) in the
// forked project. price/stockQuantity are still here for now, unchanged - they get
// removed once PricingTier takes over per-package pricing (see PLAN.md Phase 4).
@Entity
@Table(name = "ad_space_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdSpacePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column()
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Location location;

    @Column(nullable = false)
    private boolean active = true;
}