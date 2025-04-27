package com.juan.curso.springboot.crud.crud_springboot.dto.products;

import com.juan.curso.springboot.crud.crud_springboot.entities.products.ProductCategory;

public class ProductRequestDTO {

    private String name;

    private String brand;

    private Double price;

    private String description;

    private ProductCategory category;

    public ProductRequestDTO(String name, String brand, Double price, String description, ProductCategory category) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.description = description;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
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

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", category=" + category ;
    }

}
