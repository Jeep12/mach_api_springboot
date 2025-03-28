package com.juan.curso.springboot.crud.crud_springboot;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
	@PropertySource("classpath:messages.properties")
})
public class AppConfig {

}
