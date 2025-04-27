package com.juan.curso.springboot.crud.crud_springboot.services.products;

import java.util.List;
import java.util.Map;

import com.juan.curso.springboot.crud.crud_springboot.dto.categories.CategoryRequestDTO;
import com.juan.curso.springboot.crud.crud_springboot.dto.categories.CategoryResponseDTO;

public interface CategoryService {

    Map<String, String> createCategory(CategoryRequestDTO categoryDTO);

    List<CategoryResponseDTO> getAllCategories();

    Map<String, String> updateCategory(Long categoryId, CategoryRequestDTO categoryDTO);

    Map<String, String> deleteCategory(Long categoryId);

}
