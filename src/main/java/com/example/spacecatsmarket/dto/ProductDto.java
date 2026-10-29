package com.example.spacecatsmarket.dto;

import java.util.UUID;

public record ProductDto(
        String id,
        String name,
        String description,
        Double price,
        UUID categoryId,
        String categoryName
) {}
