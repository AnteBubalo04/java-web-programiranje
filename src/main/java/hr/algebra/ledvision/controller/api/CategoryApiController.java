package hr.algebra.ledvision.controller.api;

import hr.algebra.ledvision.dto.CategoryDto;
import hr.algebra.ledvision.model.Category;
import hr.algebra.ledvision.repository.CategoryRepository;
import hr.algebra.ledvision.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static hr.algebra.ledvision.mapper.CategoryMapper.toDto;
import static hr.algebra.ledvision.mapper.CategoryMapper.toEntity;


@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category management API")
public class CategoryApiController {

    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    @Operation(summary = "Get all Categories")
    public List<CategoryDto> getAllProducts() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Category by ID")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategorieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Category (ADMIN only)")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id,
                                                      @RequestBody CategoryDto categoryDto)
    {
        return categoryRepository.findById(id)
                .map(c -> {
                    c.setName(categoryDto.getName());
                    c.setDescription(categoryDto.getDescription());
                    c.setImageUrl(categoryDto.getImageUrl());
                    Category saved = categoryRepository.save(c);
                    return ResponseEntity.ok(toDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Category (ADMIN only)")
    public ResponseEntity<CategoryDto> create(@RequestBody CategoryDto dto) {
        Category category = toEntity(dto);
        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(toDto(saved));
    }



}
