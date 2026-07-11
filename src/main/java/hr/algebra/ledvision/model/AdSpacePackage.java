package hr.algebra.ledvision.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// A bookable LED-advertising offer at a single Location (e.g. "Prime evening loop -
// Ban Jelačić Square"). This used to be "Product" (a physical motorcycle part) in the
// forked project, and used to carry its own price/stockQuantity like a product would.
// Pricing now lives on PricingTier instead (duration + screen size both affect price),
// so a package itself is just the "what/where", and its PricingTier rows are the
// "how much/how long" a buyer picks from on the detail page.
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

    @Column()
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Location location;

    @Column(nullable = false)
    private boolean active = true;
}