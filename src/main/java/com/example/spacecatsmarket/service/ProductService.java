package com.example.spacecatsmarket.service;

import com.example.spacecatsmarket.dto.ProductCreateDto;
import com.example.spacecatsmarket.dto.ProductDto;
import com.example.spacecatsmarket.dto.ProductListResponseDto;
import java.util.UUID;

public interface ProductService {
    ProductListResponseDto getAllProducts(int page, int size);
    ProductDto getProductById(UUID id);
    ProductDto createProduct(ProductCreateDto dto);
    ProductDto updateProduct(UUID id, ProductCreateDto dto);
    void deleteProduct(UUID id);
}
