package com.accenture.casetoken;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CaseTokenApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(CaseTokenApplication.class, args);
		
		GeradorToken.codificar("usuario:alteração_leitura_exclusão");
	}
	
 }