package hr.algebra.ledvision.controller.mvc;

import hr.algebra.ledvision.model.AdSpacePackage;
import hr.algebra.ledvision.model.Location;
import hr.algebra.ledvision.model.Order;
import hr.algebra.ledvision.repository.LoginHistoryRepository;
import hr.algebra.ledvision.repository.UserRepository;
import hr.algebra.ledvision.service.AdExampleService;
import hr.algebra.ledvision.service.AdSpacePackageService;
import hr.algebra.ledvision.service.OrderService;
import hr.algebra.ledvision.service.PricingTierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

// Dashboard, Location CRUD, AdSpacePackage CRUD, order management and login
// history. PricingTier/AdExample CRUD (both scoped to a single package) live
// in AdminPackageMediaController instead - keeping them here would push this
// class over the assignment's 200-line-per-class limit.
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {

    private final AdSpacePackageService packageService;
    private final PricingTierService tierService;
    private final AdExampleService exampleService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final OrderService orderService;
    private final UserRepository userRepository;

    private static final String LOCATION_VIEW = "location";
    private static final String PACKAGE_VIEW = "pkg";
    private static final String ORDERS_VIEW = "orders";
    private static final String LOCATIONS_VIEW = "locations";
    private static final String PACKAGES_VIEW = "packages";
    private static final String TIERS_VIEW = "tiers";
    private static final String EXAMPLES_VIEW = "examples";
    private static final String LOGIN_HISTORY_VIEW = "loginHistory";



    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute(PACKAGES_VIEW, packageService.getAllActivePackages());
        model.addAttribute(LOCATIONS_VIEW, packageService.getAllLocations());
        return "admin/dashboard";
    }



    @GetMapping("/locations/new")
    public String newLocationForm(Model model) {
        model.addAttribute(LOCATION_VIEW, new Location());
        return "admin/location-form";
    }

    @GetMapping("/locations/edit/{id}")
    public String editLocationForm(@PathVariable Long id, Model model) {
        packageService.getLocationById(id)
                .ifPresent(location -> model.addAttribute(LOCATION_VIEW, location));
        return "admin/location-form";
    }

    @PostMapping("/locations/save")
    public String saveLocation(@ModelAttribute Location location) {
        packageService.saveLocation(location);
        return "redirect:/admin";
    }

    @PostMapping("/locations/delete/{id}")
    public String deleteLocation(@PathVariable Long id) {
        packageService.deleteLocation(id);
        return "redirect:/admin";
    }



    @GetMapping("/packages/new")
    public String newPackageForm(Model model) {
        model.addAttribute(PACKAGE_VIEW, new AdSpacePackage());
        model.addAttribute(LOCATIONS_VIEW, packageService.getAllLocations());
        return "admin/package-form";
    }

    @GetMapping("/packages/edit/{id}")
    public String editPackageForm(@PathVariable Long id, Model model) {
        packageService.getPackageById(id)
                .ifPresent(adSpacePackage -> model.addAttribute(PACKAGE_VIEW, adSpacePackage));
        model.addAttribute(LOCATIONS_VIEW, packageService.getAllLocations());
        model.addAttribute(TIERS_VIEW, tierService.getTiersByPackageId(id));
        model.addAttribute(EXAMPLES_VIEW, exampleService.getExamplesByPackageId(id));
        return "admin/package-form";
    }

    @PostMapping("/packages/save")
    public String savePackage(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) String imageUrl,
            @RequestParam Long locationId) {

        AdSpacePackage adSpacePackage = (id != null)
                ? packageService.getPackageById(id).orElse(new AdSpacePackage())
                : new AdSpacePackage();

        adSpacePackage.setName(name);
        adSpacePackage.setDescription(description);
        adSpacePackage.setImageUrl(imageUrl);

        packageService.getLocationById(locationId)
                .ifPresent(adSpacePackage::setLocation);

        packageService.savePackage(adSpacePackage);
        return "redirect:/admin";
    }

    @PostMapping("/packages/delete/{id}")
    public String deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        return "redirect:/admin";
    }


    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) Long userId,
                         @RequestParam(required = false) LocalDate from,
                         @RequestParam(required = false) LocalDate to,
                         Model model) {

        model.addAttribute(ORDERS_VIEW, orderService.getOrdersFiltered(userId,
                from != null ? from.atStartOfDay() : null,
                to != null ? to.atTime(23, 59, 59) : null));

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedFrom", from);
        model.addAttribute("selectedTo", to);
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status) {
        orderService.updateOrderStatus(id,
                Order.OrderStatus.valueOf(status));
        return "redirect:/admin/orders";
    }



    @GetMapping("/login-history")
    public String loginHistory(Model model) {
        model.addAttribute(LOGIN_HISTORY_VIEW,
                loginHistoryRepository.findAllWithUserOrderByLoggedAtDesc());
        return "admin/login-history";
    }
}