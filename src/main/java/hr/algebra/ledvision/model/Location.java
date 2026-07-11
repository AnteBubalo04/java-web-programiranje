package hr.algebra.ledvision.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// A physical LED-screen spot in Zagreb (e.g. "Ban Jelačić Square", "Cvjetni trg").
// This used to be "Category" (grouping motorcycle products) in the forked project;
// here it groups the AdSpacePackage offers available at that spot. One Location has
// many AdSpacePackages, same 1:N shape as the original Category -> Product relation.
@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // Free-text description, e.g. street address, foot-traffic notes, screen size context.
    @Column(length = 500)
    private String description;

    @Column()
    private String imageUrl;
}