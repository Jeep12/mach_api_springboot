package com.juan.curso.springboot.crud.crud_springboot.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.juan.curso.springboot.crud.crud_springboot.entities.products.Product;
@Component
public class ProductValidation implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Product.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Product product = (Product) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", null,"es requerido!");
        //ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "NotBlank.product.description");
        
        if(product.getDescription()==null || product.getDescription().isBlank()){
            errors.rejectValue("description", null,"Es requerido, por favor!");
        }
        if(product.getPrice() == null ){
            errors.rejectValue("price", null,"no puede ser nulo!");
        }else if ( product.getPrice() < 0){
            errors.rejectValue("price",null ,"no puede ser menor a 0");
        }

    }

}
