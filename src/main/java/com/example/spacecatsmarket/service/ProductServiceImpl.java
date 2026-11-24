package com.example.spacecatsmarket.service;

import com.example.spacecatsmarket.domain.Category;
import com.example.spacecatsmarket.domain.PageResult;
import com.example.spacecatsmarket.domain.Product;
import com.example.spacecatsmarket.exception.ProductNotFoundException;
import com.example.spacecatsmarket.repository.ProductRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductServiceImpl(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Override
    public PageResult<Product> getAllProducts(int page, int size) {
        return productRepository.findPage(page, size);
    }

    @Override
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public Product createProduct(Product product) {
        Category category = categoryService.getCategoryById(product.getCategory().getId());
        Product toSave = product.toBuilder()
                .id(UUID.randomUUID())
                .category(category)
                .build();
        return productRepository.save(toSave);
    }

    @Override
    public Product updateProduct(UUID id, Product changes) {
        Category category = categoryService.getCategoryById(changes.getCategory().getId());
        return productRepository.update(id, existing -> existing.toBuilder()
                        .name(changes.getName())
                        .description(changes.getDescription())
                        .price(changes.getPrice())
                        .category(category)
                        .build())
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!productRepository.deleteById(id)) {
            throw new ProductNotFoundException(id);
        }
    }
}
