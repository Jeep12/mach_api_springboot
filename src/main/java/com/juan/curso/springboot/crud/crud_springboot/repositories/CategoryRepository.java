package com.juan.curso.springboot.crud.crud_springboot.repositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.juan.curso.springboot.crud.crud_springboot.entities.products.ProductCategory;



@Repository
public interface CategoryRepository extends CrudRepository<ProductCategory, Long> {

    boolean existsByName(String name);
    


    
}
