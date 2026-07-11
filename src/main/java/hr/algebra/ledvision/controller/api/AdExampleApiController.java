package hr.algebra.ledvision.controller.api;

import hr.algebra.ledvision.dto.AdExampleDto;
import hr.algebra.ledvision.mapper.AdExampleMapper;
import hr.algebra.ledvision.model.AdExample;
import hr.algebra.ledvision.service.AdExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
@Tag(name = "Ad Examples", description = "Example ad media (images/videos) management API")
public class AdExampleApiController {

    private final AdExampleService exampleService;

    @GetMapping("/package/{packageId}")
    @Operation(summary = "Get example ads for a package")
    public List<AdExampleDto> getByPackage(@PathVariable Long packageId) {
        return exampleService.getExamplesByPackageId(packageId).stream()
                .map(AdExampleMapper::toDto)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create example ad (ADMIN only)")
    public ResponseEntity<AdExampleDto> create(@RequestBody AdExampleDto dto) {
        AdExample example = new AdExample();
        example.setMediaUrl(dto.getMediaUrl());
        example.setMediaType(dto.getMediaType());
        example.setCaption(dto.getCaption());
        exampleService.saveExample(dto.getPackageId(), example);
        return ResponseEntity.ok(AdExampleMapper.toDto(example));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete example ad (ADMIN only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exampleService.deleteExample(id);
        return ResponseEntity.noContent().build();
    }
}