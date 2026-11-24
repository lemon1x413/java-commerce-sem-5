package com.example.spacecatsmarket.mapper;

import com.example.spacecatsmarket.domain.PageResult;
import com.example.spacecatsmarket.domain.Product;
import com.example.spacecatsmarket.dto.ProductCreateDto;
import com.example.spacecatsmarket.dto.ProductDto;
import com.example.spacecatsmarket.dto.ProductListResponseDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductDto toDto(Product product);

    List<ProductDto> toDtoList(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "category.name", ignore = true)
    Product toDomain(ProductCreateDto dto);

    default ProductListResponseDto toListResponse(PageResult<Product> page) {
        return new ProductListResponseDto(
                toDtoList(page.content()),
                page.page(),
                page.size(),
                page.totalElements(),
                page.hasNext());
    }
}
