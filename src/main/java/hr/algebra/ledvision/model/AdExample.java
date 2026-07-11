package hr.algebra.ledvision.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// One sample creative (photo or short video loop) shown on a package's detail
// page, so a buyer can see what an ad on that screen actually looks like before
// booking. mediaUrl works the same way imageUrl already does elsewhere in this
// app - either a full external URL, or a local "/images/..." or "/videos/..."
// path served from src/main/resources/static (see PLAN.md Phase 5.2 for exactly
// where to drop files).
@Entity
@Table(name = "ad_examples")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private AdSpacePackage adSpacePackage;

    @Column(nullable = false, length = 500)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(length = 200)
    private String caption;

    public enum MediaType {
        IMAGE, VIDEO
    }
}