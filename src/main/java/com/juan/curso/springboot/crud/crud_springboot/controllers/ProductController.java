package com.juan.curso.springboot.crud.crud_springboot.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juan.curso.springboot.crud.crud_springboot.dto.products.ProductRequestDTO;
import com.juan.curso.springboot.crud.crud_springboot.entities.products.Product;
import com.juan.curso.springboot.crud.crud_springboot.services.products.ProductService;
import com.juan.curso.springboot.crud.crud_springboot.validation.ProductValidation;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductValidation validation;

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> list() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> view(@PathVariable Long id) {
        Optional<Product> productOptional = productService.findById(id);
        if (productOptional.isPresent()) {
            return ResponseEntity.ok(productOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();

    }

    // El BindingResult tiene que estar a la derecha del objeto que se va a validar
    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProductRequestDTO product, BindingResult result) {

        validation.validate(product, result);
        if (result.hasFieldErrors()) {
            return validation(result);
        }

        Map<String, String> response = this.productService.createProduct(product);

        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response); // Si hay error, devuelves un bad request
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(product));
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Product product, BindingResult result,
            @PathVariable Long id) {

        validation.validate(product, result);
        if (result.hasFieldErrors()) {
            return validation(result);
        }
        Optional<Product> productOptional = productService.update(id, product);
        if (productOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(productOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();

    }

    // @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        Optional<Product> productOptional = productService.delete(id);
        if (productOptional.isPresent()) {
            return ResponseEntity.ok(productOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();

    }

    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();

        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
