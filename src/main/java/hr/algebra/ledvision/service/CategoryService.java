package hr.algebra.ledvision.service;

import hr.algebra.ledvision.dto.CategoryDto;
import hr.algebra.ledvision.mapper.CategoryMapper;
import hr.algebra.ledvision.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

  public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream().map(CategoryMapper::toDto).toList();
    }

    public Optional<CategoryDto> getCategorieById(Long id) {
      return categoryRepository.findById(id).map(CategoryMapper::toDto);
    }
}
