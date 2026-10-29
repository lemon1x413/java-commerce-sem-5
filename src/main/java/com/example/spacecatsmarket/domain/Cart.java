package com.example.spacecatsmarket.domain;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Cart {
    private UUID id;
    private List<CartItem> items;
}
