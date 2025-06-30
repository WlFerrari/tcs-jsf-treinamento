package br.com.tcs.treinamento.model;

import java.util.Date;

public class EmailEnviado {
    private String destinatario;
    private String assunto;
    private String mensagem;
    private Date dataEnvio;

    public EmailEnviado(String destinatario, String assunto, String mensagem, Date dataEnvio) {
        this.destinatario = destinatario;
        this.assunto = assunto;
        this.mensagem = mensagem;
        this.dataEnvio = dataEnvio;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getAssunto() {
        return assunto;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Date getDataEnvio() {
        return dataEnvio;
    }
}
