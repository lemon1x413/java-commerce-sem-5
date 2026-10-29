package com.example.spacecatsmarket.domain;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Category {
    private UUID id;
    private String name;
}
