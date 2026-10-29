package com.example.spacecatsmarket.service;

import com.example.spacecatsmarket.domain.Category;
import com.example.spacecatsmarket.domain.Product;
import com.example.spacecatsmarket.dto.ProductCreateDto;
import com.example.spacecatsmarket.dto.ProductDto;
import com.example.spacecatsmarket.dto.ProductListResponseDto;
import com.example.spacecatsmarket.exception.ProductNotFoundException;
import com.example.spacecatsmarket.mapper.ProductMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final Map<UUID, Product> products = new LinkedHashMap<>();
    private final Map<UUID, Category> categories = new LinkedHashMap<>();

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
        initMockData();
    }

    private void initMockData() {
        Category yarnCategory = Category.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("Cosmic Yarn")
                .build();
        Category foodCategory = Category.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .name("Space Food")
                .build();
        Category toysCategory = Category.builder()
                .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .name("Galactic Toys")
                .build();

        categories.put(yarnCategory.getId(), yarnCategory);
        categories.put(foodCategory.getId(), foodCategory);
        categories.put(toysCategory.getId(), toysCategory);

        addProduct("Anti-Gravity Star Yarn", "A ball of yarn that floats in zero gravity", new BigDecimal("29.99"), yarnCategory);
        addProduct("Galaxy Milk", "Fresh milk sourced from the Milky Way", new BigDecimal("15.50"), foodCategory);
        addProduct("Comet Dust Treats", "Delicious snacks made from real comet dust", new BigDecimal("42.00"), foodCategory);
        addProduct("Nebula Laser Pointer", "Projects beautiful nebula patterns for cats to chase", new BigDecimal("89.99"), toysCategory);
        addProduct("Solar Catnip", "Catnip grown under three suns for maximum potency", new BigDecimal("19.95"), foodCategory);
    }

    private void addProduct(String name, String description, BigDecimal price, Category category) {
        UUID id = UUID.randomUUID();
        Product product = Product.builder()
                .id(id)
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .build();
        products.put(id, product);
    }

    @Override
    public ProductListResponseDto getAllProducts(int page, int size) {
        List<Product> allProducts = new ArrayList<>(products.values());
        int totalElements = allProducts.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<Product> pageContent;
        if (fromIndex >= totalElements) {
            pageContent = List.of();
        } else {
            pageContent = allProducts.subList(fromIndex, toIndex);
        }

        return new ProductListResponseDto(
                productMapper.toDtoList(pageContent),
                page,
                size,
                totalElements
        );
    }

    @Override
    public ProductDto getProductById(UUID id) {
        Product product = products.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return productMapper.toDto(product);
    }

    @Override
    public ProductDto createProduct(ProductCreateDto dto) {
        Product product = productMapper.toDomain(dto);
        UUID id = UUID.randomUUID();
        product.setId(id);

        Category category = categories.get(dto.categoryId());
        if (category == null) {
            category = Category.builder()
                    .id(dto.categoryId())
                    .name("General Cosmic Goods")
                    .build();
            categories.put(category.getId(), category);
        }
        product.setCategory(category);

        products.put(id, product);
        return productMapper.toDto(product);
    }

    @Override
    public ProductDto updateProduct(UUID id, ProductCreateDto dto) {
        Product existing = products.get(id);
        if (existing == null) {
            throw new ProductNotFoundException(id);
        }
        existing.setName(dto.name());
        existing.setDescription(dto.description());
        existing.setPrice(dto.price());

        Category category = categories.get(dto.categoryId());
        if (category == null) {
            category = Category.builder()
                    .id(dto.categoryId())
                    .name("General Cosmic Goods")
                    .build();
            categories.put(category.getId(), category);
        }
        existing.setCategory(category);

        return productMapper.toDto(existing);
    }

    @Override
    public void deleteProduct(UUID id) {
        Product existing = products.remove(id);
        if (existing == null) {
            throw new ProductNotFoundException(id);
        }
    }
}
