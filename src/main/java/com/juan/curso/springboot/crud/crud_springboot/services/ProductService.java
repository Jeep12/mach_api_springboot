package com.juan.curso.springboot.crud.crud_springboot.services;

import java.util.List;
import java.util.Optional;

import com.juan.curso.springboot.crud.crud_springboot.entities.Product;

public interface ProductService {

    List<Product>findAll();

    Optional<Product>findById(Long id);

    Product save(Product product);
    
    Optional<Product> update(Long id,Product product);

    Optional<Product> delete(Long id);


}
