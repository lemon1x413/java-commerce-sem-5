package com.example.spacecatsmarket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.spacecatsmarket.domain.Category;
import com.example.spacecatsmarket.exception.CategoryNotFoundException;
import com.example.spacecatsmarket.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl")
class CategoryServiceImplTest {

    private static final Category FOOD = Category.builder().id(UUID.randomUUID()).name("Space Food").build();

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void getAllCategoriesReturnsRepositoryList() {
        when(categoryRepository.findAll()).thenReturn(List.of(FOOD));

        assertThat(categoryService.getAllCategories()).containsExactly(FOOD);
    }

    @Test
    void getCategoryByIdReturnsCategory() {
        when(categoryRepository.findById(FOOD.getId())).thenReturn(Optional.of(FOOD));

        assertThat(categoryService.getCategoryById(FOOD.getId())).isEqualTo(FOOD);
    }

    @Test
    void getCategoryByIdThrowsWhenMissing() {
        UUID unknown = UUID.randomUUID();
        when(categoryRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(unknown))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining(unknown.toString());
    }
}
