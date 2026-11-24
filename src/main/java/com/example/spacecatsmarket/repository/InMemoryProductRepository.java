package com.example.spacecatsmarket.repository;

import com.example.spacecatsmarket.domain.Category;
import com.example.spacecatsmarket.domain.PageResult;
import com.example.spacecatsmarket.domain.Product;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Repository;

/**
 * Mock product storage until a real database is wired in.
 *
 * <p>Thread safety: the {@link LinkedHashMap} keeps insertion order, which makes paging
 * repeatable, but it is not safe for concurrent use on its own. Every access goes through
 * a {@link ReadWriteLock}: reads (paging, lookups) share the read lock and may run in
 * parallel; writes (save, update, delete) take the exclusive write lock. A page is built
 * entirely under one read lock, so its content and totalElements always agree, and no
 * iteration can ever see a concurrent modification.
 *
 * <p>Stored products are never handed out directly: callers get copies, and updates put a
 * new instance into the map instead of mutating the shared one.
 */
@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<UUID, Product> products = new LinkedHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public InMemoryProductRepository(CategoryRepository categoryRepository) {
        Category yarn = categoryRepository.findById(InMemoryCategoryRepository.COSMIC_YARN_ID).orElseThrow();
        Category food = categoryRepository.findById(InMemoryCategoryRepository.SPACE_FOOD_ID).orElseThrow();
        Category toys = categoryRepository.findById(InMemoryCategoryRepository.GALACTIC_TOYS_ID).orElseThrow();

        seed("Anti-Gravity Star Yarn", "A ball of yarn that floats in zero gravity", "29.99", yarn);
        seed("Galaxy Milk", "Fresh milk sourced from the Milky Way", "15.50", food);
        seed("Comet Dust Treats", "Delicious snacks made from real comet dust", "42.00", food);
        seed("Nebula Laser Pointer", "Projects beautiful nebula patterns for cats to chase", "89.99", toys);
        seed("Solar Catnip", "Catnip grown under three suns for maximum potency", "19.95", food);
    }

    private void seed(String name, String description, String price, Category category) {
        save(Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .category(category)
                .build());
    }

    @Override
    public PageResult<Product> findPage(int page, int size) {
        lock.readLock().lock();
        try {
            List<Product> content = products.values().stream()
                    .skip((long) page * size)
                    .limit(size)
                    .map(InMemoryProductRepository::copy)
                    .toList();
            return new PageResult<>(content, page, size, products.size());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Product> findById(UUID id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(products.get(id)).map(InMemoryProductRepository::copy);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Product save(Product product) {
        Product stored = copy(product);
        lock.writeLock().lock();
        try {
            products.put(stored.getId(), stored);
        } finally {
            lock.writeLock().unlock();
        }
        return copy(stored);
    }

    @Override
    public Optional<Product> update(UUID id, UnaryOperator<Product> updater) {
        lock.writeLock().lock();
        try {
            Product existing = products.get(id);
            if (existing == null) {
                return Optional.empty();
            }
            Product updated = copy(updater.apply(copy(existing)));
            updated.setId(id);
            products.put(id, updated);
            return Optional.of(copy(updated));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean deleteById(UUID id) {
        lock.writeLock().lock();
        try {
            return products.remove(id) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static Product copy(Product product) {
        return product.toBuilder().build();
    }
}
