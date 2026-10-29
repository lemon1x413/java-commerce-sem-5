package com.example.spacecatsmarket.dto;

import java.util.List;

public record ProductListResponseDto(
        List<ProductDto> content,
        int page,
        int size,
        long totalElements
) {}
