package hr.algebra.ledvision.controller.mvc;

import hr.algebra.ledvision.service.AdSpacePackageService;
import hr.algebra.ledvision.service.PricingTierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AdSpacePackageViewController {

    private final AdSpacePackageService packageService;
    private final PricingTierService tierService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("locations", packageService.getAllLocations());
        model.addAttribute("packages", packageService.getAllActivePackages());
        return "index";
    }

    @GetMapping("/packages")
    public String packages(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String search,
            Model model) {

        if (search != null && !search.isEmpty()) {
            model.addAttribute("packages", packageService.searchPackages(search));
        } else if (locationId != null) {
            model.addAttribute("packages", packageService.getPackagesByLocation(locationId));
        } else {
            model.addAttribute("packages", packageService.getAllActivePackages());
        }

        model.addAttribute("locations", packageService.getAllLocations());
        model.addAttribute("selectedLocation", locationId);
        model.addAttribute("search", search);
        return "packages/list";
    }

    @GetMapping("/packages/{id}")
    public String packageDetail(@PathVariable Long id, Model model) {
        packageService.getPackageById(id)
                .ifPresent(adSpacePackage -> model.addAttribute("pkg", adSpacePackage));
        model.addAttribute("tiers", tierService.getTiersByPackageId(id));
        return "packages/detail";
    }
}