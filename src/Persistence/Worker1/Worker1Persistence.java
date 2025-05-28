/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Persistence.Worker1;

import java.util.List;

/**
 *
 * @author Mauros
 */
public class Worker1Persistence {

    public List<Worker1Persistence.SessionValues> session;
    public String workspace;

    public static class SessionValues {

// [HTTP]
        public String http_Endpoint;
        public int http_Operacao;
        public int http_Protocolo;
        public String http_Parametros;
        public String http_Url;

// [TELNET]
        public String telnet_Servidor;
        public String telnet_Instrucao;
        public String telnet_Porta;

// [SSH]
        public String ssh_Servidor;
        public String ssh_Instrucao;
        public String ssh_Porta;
        public String ssh_Usuario;
        public String ssh_Senha;

// [SOCKET]
        public String socket_Servidor;
        public String socket_Porta;

// [SMTP]
        public String smtp_Servidor;
        public String smtp_Porta;
        public String smtp_Password;
        public String smtp_Protocolo;
        public String smtp_Remetente;
        public String smtp_Destinatario;
        public String smtp_Titulo;
        public String smtp_CorpoEmail;
        public boolean smtp_IsStartTls;
        public boolean smtp_IsRelay;
        public boolean smtp_IsAutenticacao;

// [SMB]
        public String smb_Servidor;
        public int smb_Operacao;
        public String smb_Password;
        public String smb_Protocolo;
        public String smb_Dominio;
        public String smb_Usuario;
        public String smb_Diretorio;
        public String smb_ValorAnterior;
        public String smb_NovoValor;
        public String smb_Conteudo;

// [NTP]
        public String ntp_Servidor;

// [ICMP]
        public String icmp_Servidor;
        public String icmp_Quantidade;

// [FTP]
        public String ftp_Servidor;
        public String ftp_Porta;
        public int ftp_Operacao;
        public String ftp_Password;
        public String ftp_Protocolo;
        public String ftp_Usuario;
        public String ftp_DiretorioRemoto;
        public String ftp_DiretorioAtual;

// [DNS]
        public String dns_Servidor;
        public String dns_Dominio;
        public String dns_Tipo;
        public String dns_Classe;
    }

}
