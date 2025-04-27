package com.juan.curso.springboot.crud.crud_springboot.entities.products;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "product_category")
public class ProductCategory {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

  
    @NotBlank(message = "es obligatorio")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "es obligatorio")
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updateAt;

    @OneToMany(mappedBy = "category")
    private List<Product> products;

    public ProductCategory(){
        
    }
    public ProductCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.updateAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = new Date();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }
}
