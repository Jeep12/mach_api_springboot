package com.juan.curso.springboot.crud.crud_springboot.services.products;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.juan.curso.springboot.crud.crud_springboot.dto.products.ProductRequestDTO;
import com.juan.curso.springboot.crud.crud_springboot.entities.products.Product;

public interface ProductService {

    List<Product> findAll();

    Optional<Product> findById(Long id);

    Product save(Product product);

    Optional<Product> update(Long id, Product product);

    Optional<Product> delete(Long id);

    Map<String, String> createProduct(ProductRequestDTO productDto);
 
}
