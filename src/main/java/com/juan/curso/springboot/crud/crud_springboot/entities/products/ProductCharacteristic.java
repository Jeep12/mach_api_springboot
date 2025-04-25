package com.juan.curso.springboot.crud.crud_springboot.entities.products;

import jakarta.persistence.*;

@Entity
@Table(name = "product_characteristics")
public class ProductCharacteristic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Nombre de la característica, como "Potencia", "Peso", "Conectividad", etc.

    private String value; // Valor de la característica, como "20W", "1.5kg", "Bluetooth 5.0", etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Relación con el producto al que pertenece la característica

    // Getters y Setters
    public Long getId() {
        return id;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
