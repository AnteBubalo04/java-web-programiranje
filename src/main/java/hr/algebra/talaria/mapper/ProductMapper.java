package hr.algebra.talaria.mapper;

import hr.algebra.talaria.dto.ProductDto;
import hr.algebra.talaria.model.Product;


public class ProductMapper {

    private ProductMapper() {}

    public static ProductDto toDto(Product p) {
        return new ProductDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStockQuantity(),
                p.getImageUrl(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null
        );
    }

    public static Product toEntity(ProductDto dto) {
        Product p = new Product();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStockQuantity(dto.getStockQuantity());
        p.setImageUrl(dto.getImageUrl());
        return p;
    }
}