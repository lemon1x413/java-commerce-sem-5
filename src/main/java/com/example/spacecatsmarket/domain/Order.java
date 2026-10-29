package com.example.spacecatsmarket.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Order {
    private UUID id;
    private List<OrderEntry> entries;
    private LocalDateTime createdAt;
}
