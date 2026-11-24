package com.example.spacecatsmarket.repository;

import com.example.spacecatsmarket.domain.PageResult;
import com.example.spacecatsmarket.domain.Product;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

public interface ProductRepository {

    PageResult<Product> findPage(int page, int size);

    Optional<Product> findById(UUID id);

    Product save(Product product);

    /**
     * Atomically replaces the product with {@code updater}'s result.
     * Returns empty when no product has this id, so a concurrent delete is never undone.
     */
    Optional<Product> update(UUID id, UnaryOperator<Product> updater);

    boolean deleteById(UUID id);
}
