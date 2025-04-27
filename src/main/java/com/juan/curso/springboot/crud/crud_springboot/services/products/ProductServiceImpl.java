package com.juan.curso.springboot.crud.crud_springboot.services.products;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juan.curso.springboot.crud.crud_springboot.dto.products.ProductRequestDTO;
import com.juan.curso.springboot.crud.crud_springboot.entities.products.Product;
import com.juan.curso.springboot.crud.crud_springboot.repositories.ProductRepository;

import jakarta.validation.Valid;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Product> findAll() {
        // Se castea porque findAll devuelve un iterable
        return (List<Product>) this.productRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Product> findById(Long id) {
        return this.productRepository.findById(id);
    }

    @Transactional
    @Override
    public Product save(Product product) {
        return this.productRepository.save(product);
    }

    @Transactional
    @Override
    public Map<String, String> createProduct(ProductRequestDTO productDto) {
        Map<String, String> response = new HashMap<>();

        boolean existsByName = this.productRepository.existsByName(productDto.getName());
        if (existsByName) {
            response.put("error", "El producto con el nombre '" + productDto.getName() + "' ya existe.");
            return response;
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCategory(productDto.getCategory());

        // Guardar el producto en la base de datos
        productRepository.save(product);
        response.put("success", "Producto creado correctamente.");

        return response;
    }

    @Transactional
    @Override
    public Optional<Product> update(Long id, Product product) {

        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isPresent()) {

            Product productDb = productOptional.orElseThrow();

            productDb.setName(product.getName());
            productDb.setPrice(product.getPrice());
            productDb.setDescription(product.getDescription());

            return Optional.of(productRepository.save(productDb));

        }
        return productOptional;
    }

    @Transactional
    @Override
    public Optional<Product> delete(Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        productOptional.ifPresent(productDb -> this.productRepository.delete(productDb));
        return productOptional;
    }

}
