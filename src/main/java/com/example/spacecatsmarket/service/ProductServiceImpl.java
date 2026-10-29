package com.example.spacecatsmarket.service;

import com.example.spacecatsmarket.domain.Category;
import com.example.spacecatsmarket.domain.Product;
import com.example.spacecatsmarket.dto.ProductCreateDto;
import com.example.spacecatsmarket.dto.ProductDto;
import com.example.spacecatsmarket.dto.ProductListResponseDto;
import com.example.spacecatsmarket.exception.ProductNotFoundException;
import com.example.spacecatsmarket.mapper.ProductMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final Map<String, Product> products = new LinkedHashMap<>();

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
        initMockData();
    }

    private void initMockData() {
        Category yarnCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Cosmic Yarn")
                .build();
        Category foodCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Space Food")
                .build();
        Category toysCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Galactic Toys")
                .build();

        addProduct("Anti-Gravity Star Yarn", "A ball of yarn that floats in zero gravity", 29.99, yarnCategory);
        addProduct("Galaxy Milk", "Fresh milk sourced from the Milky Way", 15.50, foodCategory);
        addProduct("Comet Dust Treats", "Delicious snacks made from real comet dust", 42.00, foodCategory);
        addProduct("Nebula Laser Pointer", "Projects beautiful nebula patterns for cats to chase", 89.99, toysCategory);
        addProduct("Solar Catnip", "Catnip grown under three suns for maximum potency", 19.95, foodCategory);
    }

    private void addProduct(String name, String description, double price, Category category) {
        String id = UUID.randomUUID().toString();
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
    public ProductDto getProductById(String id) {
        Product product = products.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return productMapper.toDto(product);
    }

    @Override
    public ProductDto createProduct(ProductCreateDto dto) {
        Product product = productMapper.toDomain(dto);
        String id = UUID.randomUUID().toString();
        product.setId(id);
        products.put(id, product);
        return productMapper.toDto(product);
    }

    @Override
    public ProductDto updateProduct(String id, ProductCreateDto dto) {
        Product existing = products.get(id);
        if (existing == null) {
            throw new ProductNotFoundException(id);
        }
        existing.setName(dto.name());
        existing.setDescription(dto.description());
        existing.setPrice(dto.price());
        existing.getCategory().setId(dto.categoryId());
        return productMapper.toDto(existing);
    }

    @Override
    public void deleteProduct(String id) {
        products.remove(id);
    }
}
