package com.juan.curso.springboot.crud.crud_springboot.entities.products;

import jakarta.persistence.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "es obligatorio")
    @NotEmpty
    @Size(min = 4, max = 255)
    private String name;

    @NotBlank(message = "es obligatorio")
    @NotEmpty
    private String brand;

    @NotBlank(message = "es obligatorio")
    @Min(value = 0, message = "{Min.product.price}")
    private Double price;

    @NotBlank(message = "es obligatorio")
    @NotEmpty
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductVariation> variations;
   
   
    public Product(){

    }


    public Product(String name, String brand, Double price, String description, ProductCategory category) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.description = description;
        this.category = category;
    }


    

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }


    public void setBrand(String brand) {
        this.brand = brand;
    }


    public ProductCategory getCategory() {
        return category;
    }


    public void setCategory(ProductCategory category) {
        this.category = category;
    }


    public List<ProductVariation> getVariations() {
        return variations;
    }


    public void setVariations(List<ProductVariation> variations) {
        this.variations = variations;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
