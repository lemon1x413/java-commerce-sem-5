package com.example.spacecatsmarket.repository;

import com.example.spacecatsmarket.domain.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Mock category catalogue until a real database is wired in.
 * The list is immutable, so concurrent reads need no locking.
 */
@Repository
public class InMemoryCategoryRepository implements CategoryRepository {

    public static final UUID COSMIC_YARN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID SPACE_FOOD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID GALACTIC_TOYS_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private final List<Category> categories = List.of(
            Category.builder().id(COSMIC_YARN_ID).name("Cosmic Yarn").build(),
            Category.builder().id(SPACE_FOOD_ID).name("Space Food").build(),
            Category.builder().id(GALACTIC_TOYS_ID).name("Galactic Toys").build());

    @Override
    public List<Category> findAll() {
        return categories;
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return categories.stream()
                .filter(category -> category.getId().equals(id))
                .findFirst();
    }
}
