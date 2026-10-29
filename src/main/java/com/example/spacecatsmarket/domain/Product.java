package com.example.spacecatsmarket.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Product {
    private String id;
    private String name;
    private String description;
    private Double price;
    private Category category;
}
