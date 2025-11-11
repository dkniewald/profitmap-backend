package com.profitmap_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ProfitmapBackendApplication {

	public ProfitmapBackendApplication(GenericHttpMessageConverter genericHttpMessageConverter) {
	}

	public static void main(String[] args) {
		SpringApplication.run(ProfitmapBackendApplication.class, args);
	}

	@GetMapping
	public String helloWorld() {
		return "Hello World!";
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
