package com.juan.curso.springboot.crud.crud_springboot.repositories;

import org.springframework.data.repository.CrudRepository;
import com.juan.curso.springboot.crud.crud_springboot.entities.products.Product;

public interface ProductRepository extends CrudRepository<Product, Long> {

}
