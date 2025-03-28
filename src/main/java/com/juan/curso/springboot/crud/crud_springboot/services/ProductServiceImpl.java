package com.juan.curso.springboot.crud.crud_springboot.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juan.curso.springboot.crud.crud_springboot.entities.Product;
import com.juan.curso.springboot.crud.crud_springboot.repositories.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository repository;

    @Transactional(readOnly = true)
    @Override
    public List<Product> findAll() {
        // Se castea porque findAll devuelve un iterable
        return (List<Product>) this.repository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Product> findById(Long id) {
        return this.repository.findById(id);
    }

    @Transactional
    @Override
    public Product save(Product product) {
        return this.repository.save(product);
    }

    @Transactional
    @Override
    public Optional<Product> update(Long id, Product product) {
        
        Optional<Product> productOptional = repository.findById(id);
        if (productOptional.isPresent()) {

            Product productDb = productOptional.orElseThrow();

            productDb.setName(product.getName());
            productDb.setPrice(product.getPrice());
            productDb.setDescription(product.getDescription());

            return Optional.of(repository.save(productDb));

        }
        return productOptional;
    }

    @Transactional
    @Override
    public Optional<Product> delete(Long id) {
        Optional<Product> productOptional = repository.findById(id);
        productOptional.ifPresent(productDb -> this.repository.delete(productDb));
        return productOptional;
    }

}
