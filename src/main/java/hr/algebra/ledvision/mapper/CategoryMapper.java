package hr.algebra.ledvision.mapper;

import hr.algebra.ledvision.dto.CategoryDto;
import hr.algebra.ledvision.model.Category;


public class CategoryMapper {

   private CategoryMapper(){}

    public static CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        return dto;
    }

    public static Category toEntity(CategoryDto dto) {
        Category c = new Category();
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        c.setImageUrl(dto.getImageUrl());
        return c;
    }

}
