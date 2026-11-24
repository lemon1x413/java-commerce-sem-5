package com.example.spacecatsmarket.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.spacecatsmarket.domain.PageResult;
import com.example.spacecatsmarket.domain.Product;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class InMemoryProductRepositoryTest {

    private final InMemoryProductRepository repository =
            new InMemoryProductRepository(new InMemoryCategoryRepository());

    @Test
    void hasNextIsFalseOnlyOnLastPage() {
        // seed data has 5 products
        assertTrue(repository.findPage(0, 2).hasNext());
        assertTrue(repository.findPage(1, 2).hasNext());
        PageResult<Product> last = repository.findPage(2, 2);
        assertFalse(last.hasNext());
        assertEquals(1, last.content().size());
        assertFalse(repository.findPage(0, 5).hasNext());
        assertTrue(repository.findPage(5, 2).content().isEmpty());
    }

    @Test
    void pagingStaysConsistentWhileOtherThreadsWrite() throws Exception {
        int writers = 4;
        ExecutorService pool = Executors.newFixedThreadPool(writers + 1);
        try {
            List<Future<?>> tasks = new ArrayList<>();
            for (int w = 0; w < writers; w++) {
                tasks.add(pool.submit(() -> {
                    for (int i = 0; i < 2_000; i++) {
                        UUID id = repository.save(newProduct()).getId();
                        repository.deleteById(id);
                    }
                }));
            }
            tasks.add(pool.submit(() -> {
                for (int i = 0; i < 2_000; i++) {
                    // size > max possible total, so one page must hold everything that exists
                    PageResult<Product> page = repository.findPage(0, 100);
                    assertEquals(page.totalElements(), page.content().size());
                }
            }));
            for (Future<?> task : tasks) {
                task.get(30, TimeUnit.SECONDS); // rethrows ConcurrentModificationException or assertion failures
            }
        } finally {
            pool.shutdownNow();
        }
        assertEquals(5, repository.findPage(0, 100).totalElements());
    }

    private static Product newProduct() {
        return Product.builder()
                .id(UUID.randomUUID())
                .name("Test Yarn")
                .price(BigDecimal.ONE)
                .build();
    }
}
