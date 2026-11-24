package com.example.spacecatsmarket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.spacecatsmarket.domain.Category;
import com.example.spacecatsmarket.domain.PageResult;
import com.example.spacecatsmarket.domain.Product;
import com.example.spacecatsmarket.exception.CategoryNotFoundException;
import com.example.spacecatsmarket.exception.ProductNotFoundException;
import com.example.spacecatsmarket.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl")
class ProductServiceImplTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final Category YARN = Category.builder().id(UUID.randomUUID()).name("Cosmic Yarn").build();

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @Captor
    private ArgumentCaptor<UnaryOperator<Product>> updaterCaptor;

    @Test
    void getAllProductsReturnsRepositoryPage() {
        PageResult<Product> page = new PageResult<>(List.of(product(PRODUCT_ID)), 0, 10, 1);
        when(productRepository.findPage(0, 10)).thenReturn(page);

        assertThat(productService.getAllProducts(0, 10)).isSameAs(page);
    }

    @Nested
    class GetProductById {

        @Test
        void returnsProductWhenFound() {
            Product product = product(PRODUCT_ID);
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            assertThat(productService.getProductById(PRODUCT_ID)).isEqualTo(product);
        }

        @Test
        void throwsWhenMissing() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(PRODUCT_ID))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining(PRODUCT_ID.toString());
        }
    }

    @Nested
    class CreateProduct {

        @Test
        void assignsNewIdAndResolvedCategory() {
            when(categoryService.getCategoryById(YARN.getId())).thenReturn(YARN);
            when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Product input = product(null).toBuilder()
                    .category(Category.builder().id(YARN.getId()).build()) // id only, as the mapper sends it
                    .build();
            Product created = productService.createProduct(input);

            verify(productRepository).save(productCaptor.capture());
            Product saved = productCaptor.getValue();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCategory()).isEqualTo(YARN);
            assertThat(saved.getName()).isEqualTo(input.getName());
            assertThat(created).isEqualTo(saved);
        }

        @Test
        void rejectsUnknownCategoryWithoutSaving() {
            UUID unknown = UUID.randomUUID();
            when(categoryService.getCategoryById(unknown)).thenThrow(new CategoryNotFoundException(unknown));

            Product input = product(null).toBuilder().category(Category.builder().id(unknown).build()).build();

            assertThatThrownBy(() -> productService.createProduct(input))
                    .isInstanceOf(CategoryNotFoundException.class);
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateProduct {

        @Test
        void appliesChangesButKeepsId() {
            when(categoryService.getCategoryById(YARN.getId())).thenReturn(YARN);
            when(productRepository.update(eq(PRODUCT_ID), updaterCaptor.capture()))
                    .thenAnswer(inv -> Optional.of(updaterCaptor.getValue().apply(product(PRODUCT_ID))));

            Product changes = Product.builder()
                    .name("Solar Yarn")
                    .description("new")
                    .price(new BigDecimal("1.50"))
                    .category(Category.builder().id(YARN.getId()).build())
                    .build();
            Product updated = productService.updateProduct(PRODUCT_ID, changes);

            assertThat(updated.getId()).isEqualTo(PRODUCT_ID);
            assertThat(updated.getName()).isEqualTo("Solar Yarn");
            assertThat(updated.getDescription()).isEqualTo("new");
            assertThat(updated.getPrice()).isEqualByComparingTo("1.50");
            assertThat(updated.getCategory()).isEqualTo(YARN);
        }

        @Test
        void throwsWhenMissing() {
            when(categoryService.getCategoryById(YARN.getId())).thenReturn(YARN);
            when(productRepository.update(eq(PRODUCT_ID), any())).thenReturn(Optional.empty());

            Product changes = product(null);

            assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, changes))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    class DeleteProduct {

        @Test
        void deletesExisting() {
            when(productRepository.deleteById(PRODUCT_ID)).thenReturn(true);

            productService.deleteProduct(PRODUCT_ID);

            verify(productRepository).deleteById(PRODUCT_ID);
        }

        @Test
        void throwsWhenMissing() {
            when(productRepository.deleteById(PRODUCT_ID)).thenReturn(false);

            assertThatThrownBy(() -> productService.deleteProduct(PRODUCT_ID))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    private static Product product(UUID id) {
        return Product.builder()
                .id(id)
                .name("Star Yarn")
                .description("floats")
                .price(new BigDecimal("29.99"))
                .category(YARN)
                .build();
    }
}
