package hr.algebra.ledvision.controller.mvc;

import hr.algebra.ledvision.model.AdExample;
import hr.algebra.ledvision.model.PricingTier;
import hr.algebra.ledvision.service.AdExampleService;
import hr.algebra.ledvision.service.AdSpacePackageService;
import hr.algebra.ledvision.service.PricingTierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

// PricingTier and AdExample CRUD, both reached from a package's edit page since
// a tier/example always belongs to exactly one package - split out of
// AdminViewController to keep each controller under the assignment's
// 200-line-per-class limit.
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPackageMediaController {

    private final AdSpacePackageService packageService;
    private final PricingTierService tierService;
    private final AdExampleService exampleService;

    private static final String TIER_VIEW = "tier";
    private static final String EXAMPLE_VIEW = "example";


    @GetMapping("/packages/{packageId}/tiers/new")
    public String newTierForm(@PathVariable Long packageId, Model model) {
        PricingTier tier = new PricingTier();
        packageService.getPackageById(packageId).ifPresent(tier::setAdSpacePackage);
        model.addAttribute(TIER_VIEW, tier);
        return "admin/tier-form";
    }

    @GetMapping("/tiers/edit/{id}")
    public String editTierForm(@PathVariable Long id, Model model) {
        tierService.getTierById(id)
                .ifPresent(tier -> model.addAttribute(TIER_VIEW, tier));
        return "admin/tier-form";
    }

    @PostMapping("/tiers/save")
    public String saveTier(
            @RequestParam(required = false) Long id,
            @RequestParam Long packageId,
            @RequestParam String durationLabel,
            @RequestParam String sizeLabel,
            @RequestParam BigDecimal price) {

        PricingTier tier = (id != null)
                ? tierService.getTierById(id).orElse(new PricingTier())
                : new PricingTier();

        tier.setDurationLabel(durationLabel);
        tier.setSizeLabel(sizeLabel);
        tier.setPrice(price);

        tierService.saveTier(packageId, tier);
        return "redirect:/admin/packages/edit/" + packageId;
    }

    @PostMapping("/tiers/delete/{id}")
    public String deleteTier(@PathVariable Long id, @RequestParam Long packageId) {
        tierService.deleteTier(id);
        return "redirect:/admin/packages/edit/" + packageId;
    }



    @GetMapping("/packages/{packageId}/examples/new")
    public String newExampleForm(@PathVariable Long packageId, Model model) {
        AdExample example = new AdExample();
        packageService.getPackageById(packageId).ifPresent(example::setAdSpacePackage);
        model.addAttribute(EXAMPLE_VIEW, example);
        return "admin/example-form";
    }

    @GetMapping("/examples/edit/{id}")
    public String editExampleForm(@PathVariable Long id, Model model) {
        exampleService.getExampleById(id)
                .ifPresent(example -> model.addAttribute(EXAMPLE_VIEW, example));
        return "admin/example-form";
    }

    @PostMapping("/examples/save")
    public String saveExample(
            @RequestParam(required = false) Long id,
            @RequestParam Long packageId,
            @RequestParam String mediaUrl,
            @RequestParam AdExample.MediaType mediaType,
            @RequestParam(required = false) String caption) {

        AdExample example = (id != null)
                ? exampleService.getExampleById(id).orElse(new AdExample())
                : new AdExample();

        example.setMediaUrl(mediaUrl);
        example.setMediaType(mediaType);
        example.setCaption(caption);

        exampleService.saveExample(packageId, example);
        return "redirect:/admin/packages/edit/" + packageId;
    }

    @PostMapping("/examples/delete/{id}")
    public String deleteExample(@PathVariable Long id, @RequestParam Long packageId) {
        exampleService.deleteExample(id);
        return "redirect:/admin/packages/edit/" + packageId;
    }
}