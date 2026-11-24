package com.example.spacecatsmarket.repository;

import com.example.spacecatsmarket.domain.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    List<Category> findAll();

    Optional<Category> findById(UUID id);
}
