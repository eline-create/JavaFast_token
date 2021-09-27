package com.accenture.casetoken;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Token {
		
	@Id
	private String tokenAcesso;
	private String credencial; 
	
	private String reativarToken;
	private int expira;
	private String tipo;
	private LocalDateTime data;
	private String situacao;
	
	public Token(){
		
	}
	
	public Token(String credencial, String tokenAcesso, String reativarToken, int expira, String tipo,
			LocalDateTime data, String situacao) {
		super();
		this.credencial = credencial;
		this.tokenAcesso = tokenAcesso;
		this.reativarToken = reativarToken;
		this.expira = expira;
		this.tipo = tipo;
		this.data = data;
		this.situacao = situacao;
	}

	public String getCredencial() {
		return credencial;
	}
	public void setCredencial(String credencial) {
		this.credencial = credencial;
	}
	public String getTokenAcesso() {
		return tokenAcesso;
	}
	public void setTokenAcesso(String tokenAcesso) {
		this.tokenAcesso = tokenAcesso;
	}
	public String getReativarToken() {
		return reativarToken;
	}
	public void setReativarToken(String reativarToken) {
		this.reativarToken = reativarToken;
	}
	public int getExpira() {
		return expira;
	}
	public void setExpira(int expira) {
		this.expira = expira;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public LocalDateTime getData() {
		return data;
	}
	public void setData(LocalDateTime data) {
		this.data = data;
	}
	public String getSituacao() {
		return situacao;
	}

	public void setSituacao(String situacao) {
		this.situacao = situacao;
	}



	@Override
	public int hashCode() {
		return Objects.hash(credencial, data, expira, reativarToken, situacao, tipo, tokenAcesso);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		return Objects.equals(credencial, other.credencial) && Objects.equals(data, other.data)
				&& expira == other.expira && Objects.equals(reativarToken, other.reativarToken)
				&& Objects.equals(situacao, other.situacao) && Objects.equals(tipo, other.tipo)
				&& Objects.equals(tokenAcesso, other.tokenAcesso);
	}

	@Override
	public String toString() {
		return "Token [credencial=" + credencial + ", tokenAcesso=" + tokenAcesso + ", reativarToken=" + reativarToken
				+ ", expira=" + expira + ", tipo=" + tipo + ", data=" + data + ", situacao=" + situacao + "]";
	}

	}
