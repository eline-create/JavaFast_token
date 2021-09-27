package com.accenture.casetoken;

import java.util.Base64;
import java.util.Random;

public class GeradorToken {
	
	/**
	 * Método para mostrar como a credencial é gerado, visto que ela será dada no momento da requisição, GerarToken.
	 * @param credencialTexto
	 * @return
	 */
	public static String codificar(String credencialTexto) {
		String credencialCodificada = Base64.getEncoder().encodeToString(credencialTexto.getBytes());
		System.out.println(credencialTexto);
		System.out.println("Basico " + credencialCodificada);
			
		return "Credencial codificada";
}
	public static String gerarToken(int length) {
		// Based on https://www.baeldung.com/java-random-string#java8-alphanumeric
				int leftLimit = 48; // numeral '0'
			    int rightLimit = 122; // letter 'z'
			    int targetStringLength = length;
			    Random random = new Random();

			    String stringGerada = random.ints(leftLimit, rightLimit + 1)
			      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
			      .limit(targetStringLength)
			      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			      .toString();

			    return stringGerada;
	}
	
}

	    
	



	    
	

