package com.juan.curso.springboot.crud.crud_springboot.services.products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.juan.curso.springboot.crud.crud_springboot.dto.categories.CategoryRequestDTO;
import com.juan.curso.springboot.crud.crud_springboot.dto.categories.CategoryResponseDTO;
import com.juan.curso.springboot.crud.crud_springboot.entities.products.ProductCategory;
import com.juan.curso.springboot.crud.crud_springboot.repositories.CategoryRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    @Override
    public Map<String, String> createCategory(CategoryRequestDTO categoryDTO) {
        Map<String, String> response = new HashMap<>();

        if (categoryRepository.existsByName(categoryDTO.getName())) {
            response.put("error", "La categoría ya existe");
            return response;
        }

        ProductCategory category = new ProductCategory(categoryDTO.getName(), categoryDTO.getDescription());
        categoryRepository.save(category);
        response.put("success", "Categoría creada correctamente");
        return response;
    }


    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        List<ProductCategory> categories = (List<ProductCategory>) categoryRepository.findAll();
        List<CategoryResponseDTO> responseList = new ArrayList<>();

        for (ProductCategory category : categories) {
            responseList.add(new CategoryResponseDTO(category.getId(), category.getName(), category.getDescription()));
        }

        return responseList;
    }

    @Transactional
    @Override
    public Map<String, String> updateCategory(Long categoryId, CategoryRequestDTO categoryDTO) {
        Map<String, String> response = new HashMap<>();

        // Verificar si la categoría existe
        ProductCategory existingCategory = categoryRepository.findById(categoryId).orElse(null);

        if (existingCategory == null) {
            response.put("error", "La categoría no existe");
            return response;
        }

        // Actualizar los campos
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());

        categoryRepository.save(existingCategory); // Guardamos los cambios
        response.put("success", "Categoría actualizada correctamente");
        return response;
    }

    @Transactional
    @Override
    public Map<String, String> deleteCategory(Long categoryId) {
        Map<String, String> response = new HashMap<>();

        // Verificar si la categoría existe
        ProductCategory existingCategory = categoryRepository.findById(categoryId).orElse(null);

        if (existingCategory == null) {
            response.put("error", "La categoría no existe");
            return response;
        }

        categoryRepository.delete(existingCategory); // Eliminar la categoría
        response.put("success", "Categoría eliminada correctamente");
        return response;
    }

}
