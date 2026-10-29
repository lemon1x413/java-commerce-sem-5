package com.example.spacecatsmarket.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class CosmicWordCheckValidator implements ConstraintValidator<CosmicWordCheck, String> {

    private static final Set<String> COSMIC_TERMS = Set.of(
            "star", "galaxy", "comet", "nebula",
            "cosmic", "lunar", "solar", "asteroid"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String lower = value.toLowerCase();
        return COSMIC_TERMS.stream().anyMatch(lower::contains);
    }
}
