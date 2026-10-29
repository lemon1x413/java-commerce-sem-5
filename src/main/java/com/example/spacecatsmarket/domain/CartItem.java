package com.example.spacecatsmarket.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItem {
    private Product product;
    private int quantity;
}
