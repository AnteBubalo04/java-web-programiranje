package hr.algebra.ledvision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Entry point for LedVision: a reservation app for transparent LED-film advertising
// space at various locations in Zagreb. The package was forked from a Spring Boot
// e-commerce demo (motorcycle parts, "Talaria") and is being re-themed step by step —
// see PLAN.md in the project root for the full sequence of changes and current status.
// Everything below this class (security/JWT, PayPal, filters, listeners, admin CRUD
// pattern) is reused unchanged from the fork; only the domain entities and the pages
// built on top of them change.
@SpringBootApplication
public class LedVisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedVisionApplication.class, args);
    }

}
