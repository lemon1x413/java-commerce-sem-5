package com.example.spacecatsmarket.service;

import com.example.spacecatsmarket.domain.Category;
import java.util.List;
import java.util.UUID;

/** Single owner of the answer to "which categories exist". */
public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(UUID id);
}
