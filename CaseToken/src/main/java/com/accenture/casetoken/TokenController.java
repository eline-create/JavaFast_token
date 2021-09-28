package com.accenture.casetoken;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/FastAcademy")

class TokenController {

	private RepositorioToken repositorio;

	TokenController(RepositorioToken repository) {
		this.repositorio = repository;
	}

	@PostMapping("/gerarToken")
	public ResponseEntity<?> gerarToken(@RequestBody Map<String, String> corpoDaRequisicao) {
		HashMap<String, String> corpoDaResposta = new HashMap<>();

		/**
		 * USANDO O TRY/CATCH
		 * https://howtodoinjava.com/java/exception-handling/try-catch-finally/
		 */
		try {

			/**
			 * VALIDAÇÃO DA CREDENCIAL/BODY DE REQUISIÇÃO PARA O POST: {
			 * "credencial":"expira" }
			 */
			if (!corpoDaRequisicao.containsKey("credencial")) {
				return new ResponseEntity<>(corpoDaResposta, HttpStatus.BAD_REQUEST);
			}

			String credencial = corpoDaRequisicao.get("credencial");

			String credencialValidacao = new String(Base64.getDecoder().decode(credencial));

			String[] validacao = credencialValidacao.split(":");
			if (validacao.length == 2) {

				credencial = "Basico " + credencialValidacao;

				int expira = 60000;
				if (corpoDaRequisicao.containsKey("expira")) {
					int inputExpira = Integer.parseInt(corpoDaRequisicao.get("expira"));
					if (inputExpira >= 0 && inputExpira < 60000) {
						expira = inputExpira;
					}
				}

				String tokenAcesso = GeradorToken.gerarToken(128);
				String reativaToken = GeradorToken.gerarToken(128);
				String tipo = "Batedor";
				LocalDateTime dataHora = LocalDateTime.now();
				String situacao = "Ativo";

				/**
				 * LIGAÇÃO COM O BANCO
				 */
				Token token = new Token(credencial, tokenAcesso, reativaToken, expira, tipo, dataHora, situacao);
				repositorio.save(token);

				corpoDaResposta.put("token_acesso", tokenAcesso);
				corpoDaResposta.put("reativa_token", reativaToken);
				corpoDaResposta.put("tipo", tipo);
				corpoDaResposta.put("expira", Integer.toString(expira));
				return new ResponseEntity<>(corpoDaResposta, HttpStatus.OK);
			} else {
				System.out.println("Credencial Invalida!" + validacao);
			}

		} catch (Exception e) {
			e.printStackTrace();
			corpoDaResposta = new HashMap<>();
			corpoDaResposta.put("mensagem", "Erro na aplicação.");
			return new ResponseEntity<>(corpoDaResposta, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(corpoDaResposta, HttpStatus.BAD_REQUEST);

	}

	/**
	 * MÉTODO PARA BUSCAR OS DADOS QUE ESTÃO SENDO ARMAZENADOS NO BANCO. IMPORTANTE:
	 * Para buscar é preciso primeiro fazer um POST, pois não há log inicial para
	 * esta API. Método criado apenas para uso interno.
	 * 
	 * @return
	 */

	@GetMapping("/buscarToken")
	List<Token> all() {
		return repositorio.findAll();
	}

	/**
	 * Buscando o token pelo token_acesso. Usando o ResponseEntity para ter os
	 * métodos de HTTP como retorno.
	 * 
	 * @param id = token_acesso
	 * 
	 * @throws Exception - criar classe para tratamento de
	 *                   erro(ExcecaoTokenNaoEncontrado) - (Quando refatorar)
	 * 
	 */

	@GetMapping("/usarToken")
	public ResponseEntity<?> usarToken(@RequestParam(value = "token_acesso", defaultValue = "0") String token_acesso) {

		/**
		 * BUSCA OS DADOS QUE ESTÃO NO BANCO
		 */
		HashMap<String, String> corpoDaResposta = new HashMap<>();

		try {/**
				 * BUSCA SE EXISTE OU NÂO, O TOKEN QUE FOI GERADO no POST, DENTRO DO BANCO DE
				 * DADOS.
				 * 
				 */

			Optional<Token> possivelToken = repositorio.findById(token_acesso);

			if (possivelToken.isPresent()) {
				Token token = possivelToken.get();
				if (token.getSituacao().equals("Expirado") || token.getSituacao().equals("Utilizado")) {
					return new ResponseEntity<>(corpoDaResposta, HttpStatus.MULTIPLE_CHOICES);
				}
				/**
				 * CÁLCULO DO TEMPO DE REQUISIÇÃO
				 * https://www.ti-enxame.com/pt/date/como-calcular-diferenca-de-horario-entre-duas-datas-usando-localdatetime-em-java-8/833280341/
				 */
				LocalDateTime horaAtual = LocalDateTime.now();
				LocalDateTime horaInicial = token.getData();
				long tempoDecorrido = Duration.between(horaInicial, horaAtual).toMillis();

				/**
				 * NÃO SOBROU TEMPO
				 */

				if (tempoDecorrido > token.getExpira()) {
					token.setSituacao("Expirado");
					repositorio.save(token);
					return new ResponseEntity<>(corpoDaResposta, HttpStatus.BAD_REQUEST);
				}
				/**
				 * SOBROU TEMPO
				 */

				if (tempoDecorrido < token.getExpira()) {
					token.setSituacao("Utilizado");
					repositorio.save(token);
					return new ResponseEntity<>(corpoDaResposta, HttpStatus.OK);
				}
				corpoDaResposta.put("expira", Long.toString(token.getExpira() - tempoDecorrido));

			}
			return new ResponseEntity<>(corpoDaResposta, HttpStatus.BAD_REQUEST);

		} catch (Exception e) {
			e.printStackTrace();
			corpoDaResposta = new HashMap<>();
			corpoDaResposta.put("mensagem", "Erro na aplicação.");
			return new ResponseEntity<>(corpoDaResposta, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/validarToken")
	public ResponseEntity<?> validarToken(
			@RequestParam(value = "token_acesso", defaultValue = "0") String token_acesso) {

		HashMap<String, String> corpoDaResposta = new HashMap<>();

		try {
			Optional<Token> possivelToken = repositorio.findById(token_acesso);

			/**
			 * MÉTODO PARA IDENTIFICAR SE O TOKEN BUSCADO ESTÁ NO BANCO DE DADOS
			 **/
			if (possivelToken.isPresent()) {
				Token token = possivelToken.get();
				if (token.getSituacao().equals("Expirado") || token.getSituacao().equals("Utilizado")) {
					return new ResponseEntity<>(corpoDaResposta, HttpStatus.MULTIPLE_CHOICES);
				}
				/**
				 * CÁLCULO DO TEMPO DA REQUISIÇÃO.
				 * */
				LocalDateTime horaAtual = LocalDateTime.now();
				LocalDateTime horaInicial = token.getData();
				long tempoDecorrido = Duration.between(horaInicial, horaAtual).toMillis();

				if (tempoDecorrido > token.getExpira()) {
					token.setSituacao("Expirado");
					repositorio.save(token);
					return new ResponseEntity<>(corpoDaResposta, HttpStatus.MULTIPLE_CHOICES);
				}
				corpoDaResposta.put("token_acesso", token_acesso);
				corpoDaResposta.put("reativa_token", token.getReativarToken());
				corpoDaResposta.put("tipo", token.getTipo());
				corpoDaResposta.put("expira", Long.toString(token.getExpira() - tempoDecorrido));

				return new ResponseEntity<>(corpoDaResposta, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(corpoDaResposta, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			e.printStackTrace();
			corpoDaResposta = new HashMap<>();
			corpoDaResposta.put("mensagem", "Erro na aplicação.");
			return new ResponseEntity<>(corpoDaResposta, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * MÉTODO PARA REATIVAR TOKEN QUE NÃO FOI UTILIZADO
	 * 
	 * @param token_acesso
	 * @param reativar_token
	 * @param expira
	 * @return
	 */

	@PutMapping("/reativarTokenExpirado")
	public ResponseEntity<?> reativarTokenExpirado(
			@RequestParam(value = "token_acesso", defaultValue = "0") String token_acesso,
			@RequestParam(value = "reativa_token", defaultValue = "0") String reativar_token,
			@RequestParam(value = "expira", defaultValue = "60000") int expira) {

		HashMap<String, String> corpoDaResposta = new HashMap<>();
		try {
			/**
			 * VALIDAR CREDENCIAL: PRECISA TER O TIPO BATEDOR
			 **/

			boolean tokenValidacao = token_acesso.contains("Batedor ");

			System.out.println(tokenValidacao);

			String[] tokenAcesso = token_acesso.split("Batedor ");
			if (tokenValidacao) {
				Optional<Token> possivelToken = repositorio.findById(tokenAcesso[1]);
				/**
				 * VALIDAR SE EXISTE O TOKEN SOLICITADO NO BANCO
				 * 
				 */
				if (possivelToken.isPresent()) {
					Token token = possivelToken.get();
					/**
					 * COMPARANDO OS TOKENS DE REATIVAÇÃO
					 */
					if (reativar_token.equals(token.getReativarToken())) {

						LocalDateTime horaAtual = LocalDateTime.now();
						LocalDateTime horaInicial = token.getData();
						long tempoDecorrido = Duration.between(horaInicial, horaAtual).toMillis();
						
						System.out.println(token.getSituacao());
						if(token.getSituacao().equals("Utilizado")) {
							return new ResponseEntity<>(corpoDaResposta, HttpStatus.MULTIPLE_CHOICES); 
						}

						if (tempoDecorrido > token.getExpira()) {
							token.setSituacao("Expirado");
							repositorio.save(token);
						}
						
						if (token.getSituacao().equals("Expirado")) {
							String novoToken = GeradorToken.gerarToken(128);

							token.setSituacao("Utilizado");
						
							repositorio.save(token);
							/**
							 * RESPONSE SOLICITADA PARA RETORNO
							 * 
							 */
							corpoDaResposta.put("token_acesso", novoToken);
							corpoDaResposta.put("tipo", token.getTipo());
							corpoDaResposta.put("expira", Integer.toString(token.getExpira()));
							return new ResponseEntity<>(corpoDaResposta, HttpStatus.OK);
						}

					} else {
						return new ResponseEntity<>(corpoDaResposta, HttpStatus.BAD_REQUEST);
					}

				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			corpoDaResposta.put("mensagem", "Erro na aplicação");
			return new ResponseEntity<>(corpoDaResposta, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(corpoDaResposta, HttpStatus.OK);
	}

	
	
	@DeleteMapping("/excluirToken")
	public ResponseEntity<?> excluirToken(
			@RequestParam(value = "token_acesso", defaultValue = "0") String token_acesso) {

		HashMap<String, String> corpoDaResposta = new HashMap<>();

		try {

			Optional<Token> possivelToken = repositorio.findById(token_acesso);
			Token token = possivelToken.get();
			/**
			 * CÁLCULO TEMPO DA REQUISIÇÃO
			 * 
			 */
			LocalDateTime horaAtual = LocalDateTime.now();
			LocalDateTime horaInicial = token.getData();
			long tempoDecorrido = Duration.between(horaInicial, horaAtual).toMillis();

			if (tempoDecorrido > token.getExpira()) {
				token.setSituacao("Expirado");
				repositorio.save(token);
			}

			if (token.getSituacao().equals("Utilizado") || token.getSituacao().equals("Expirado")) {
				repositorio.deleteById(token_acesso);
				return new ResponseEntity<>(corpoDaResposta, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(corpoDaResposta, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			corpoDaResposta.put("mensagem", "Erro na aplicação");
			return new ResponseEntity<>(corpoDaResposta, HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}
}
