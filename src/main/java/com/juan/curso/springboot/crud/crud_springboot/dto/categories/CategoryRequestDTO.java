package com.juan.curso.springboot.crud.crud_springboot.dto.categories;

public class CategoryRequestDTO {

    private String name;
    private String description;

    public CategoryRequestDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    
    public String toString(){
        String result= "";
        result+= "name: " + this.getName() ;
        result+="description: " + this.getDescription();
        return result;
    }

}
