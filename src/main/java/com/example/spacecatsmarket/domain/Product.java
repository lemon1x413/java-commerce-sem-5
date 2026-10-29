package com.example.spacecatsmarket.domain;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Product {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
}
