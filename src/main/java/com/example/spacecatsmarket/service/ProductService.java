package com.example.spacecatsmarket.service;

import com.example.spacecatsmarket.domain.PageResult;
import com.example.spacecatsmarket.domain.Product;
import java.util.UUID;

/**
 * Works with domain objects only, so it can be reused by callers other than the REST
 * controller (e.g. a scheduled job). Mapping to and from DTOs is the controller's job.
 */
public interface ProductService {
    PageResult<Product> getAllProducts(int page, int size);
    Product getProductById(UUID id);
    Product createProduct(Product product);
    Product updateProduct(UUID id, Product changes);
    void deleteProduct(UUID id);
}
