package com.juan.curso.springboot.crud.crud_springboot.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juan.curso.springboot.crud.crud_springboot.dto.categories.CategoryRequestDTO;
import com.juan.curso.springboot.crud.crud_springboot.dto.categories.CategoryResponseDTO;
import com.juan.curso.springboot.crud.crud_springboot.entities.products.Product;
import com.juan.curso.springboot.crud.crud_springboot.entities.products.ProductCategory;
import com.juan.curso.springboot.crud.crud_springboot.services.products.CategoryService;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CategoryRequestDTO category, BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }

        Map<String, String> response = categoryService.createCategory(category);

        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();

        if (categories.isEmpty()) {
            return ResponseEntity.noContent().build(); // Si no hay categorías, responde con 204 No Content
        }

        return ResponseEntity.ok(categories); // Retorna la lista de categorías
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequestDTO category, BindingResult result) {
        if (result.hasErrors()) {
            return validation(result);
        }

        Map<String, String> response = categoryService.updateCategory(categoryId, category);

        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        Map<String, String> response = categoryService.deleteCategory(categoryId);

        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();

        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

}
