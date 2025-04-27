package com.juan.curso.springboot.crud.crud_springboot.dto.categories;

public class CategoryResponseDTO {

    private Long id;
    private String name;
    private String description;

    public CategoryResponseDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
