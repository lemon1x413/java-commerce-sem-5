package com.example.spacecatsmarket.dto;

import com.example.spacecatsmarket.validation.CosmicWordCheck;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductCreateDto(
        @NotBlank @Size(min = 2, max = 100) @CosmicWordCheck
        String name,

        @Size(max = 500)
        String description,

        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        @NotNull
        UUID categoryId
) {}
