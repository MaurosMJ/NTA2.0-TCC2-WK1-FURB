/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntaworker1;

import Persistence.Worker1.Worker1Persistence;
import Persistence.JsonPersistence;
import Service.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mauros
 */
public class NtaWorker1 {

    private static int intervalInSeconds = 10;
    private final List<Worker1Persistence> MonitoringArray = new ArrayList<>();

    public NtaWorker1() {
        carregarInformacoesArquivo();
    }

    public void carregarInformacoesArquivo() {
        String nomeArquivo = "Monitoring.json";
        List<Worker1Persistence> listaMonitoramento = JsonPersistence.carregarJsonAppdataMonitoramento(nomeArquivo);

        if (listaMonitoramento == null || listaMonitoramento.isEmpty()) {
            System.out.println("Arquivo de configuração não encontrado ou inválido: " + nomeArquivo);
            return;
        }

        MonitoringArray.addAll(listaMonitoramento);
    }

    public Map<String, String> parseParametersString(String parameters) {
        Map<String, String> paramMap = new HashMap<>();

        if (parameters != null && !parameters.trim().isEmpty()) {
            String[] tokens = parameters.trim().split("\\s+");

            String currentKey = null;
            for (String token : tokens) {
                if (token.endsWith(":")) {
                    // Remover ":" do final para usar como chave
                    currentKey = token.substring(0, token.length() - 1).trim();
                } else if (currentKey != null) {
                    // A palavra seguinte é o valor
                    paramMap.put(currentKey, token.trim());
                    currentKey = null; // Resetar para esperar nova chave
                } else {
                    System.out.println("Par inválido ignorado: " + token);
                }
            }
        }

        return paramMap;
    }

    public static void main(String[] args) {
        NtaWorker1 instance = new NtaWorker1();

        Thread workerThread = new Thread(() -> {
            while (true) {
                try {
                    System.out.println("Executando tarefa...");

                    for (Worker1Persistence monitoring : instance.MonitoringArray) {
                        for (Worker1Persistence.SessionValues session : monitoring.session) {
                            try {
                                switch (monitoring.workspace.toUpperCase()) {
                                    case "HTTP":
                                        HttpClient http = new HttpClient(session.http_Protocolo, session.http_Endpoint, session.http_Parametros);
                                        if (session.http_Operacao == 0) {
                                            http.sendGetRequest();
                                            break;
                                        }
                                            Map<String, String> parametrosMap;
                                            parametrosMap = instance.parseParametersString(session.http_Parametros);
                                            http.sendPutRequest(parametrosMap);
                                        break;

                                    case "DNS":
                                        DnsClient dns = new DnsClient(session.dns_Servidor, session.dns_Dominio, session.dns_Tipo, session.dns_Classe);
                                        dns.PerformServerConnection();
                                        break;

                                    case "FTP":
                                        FtpClient ftp = new FtpClient(session.ftp_Usuario, session.ftp_Servidor, new String(session.ftp_Password), session.ftp_DiretorioRemoto, session.ftp_DiretorioAtual, session.ftp_Porta, session.ftp_Protocolo);
                                        switch (session.ftp_Operacao) {
                                            case 0:
                                                ftp.ftpAuth();
                                                break;
                                            case 1:
                                                ftp.ftpAuth();
                                                ftp.ftpDownload();
                                                break;
                                            case 2:
                                                ftp.ftpAuth();
                                                ftp.ftpUpload();
                                                break;
                                            case 3:
                                                ftp.ftpAuth();
                                                ftp.ftpListFiles();
                                                break;
                                            case 4:
                                                ftp.ftpAuth();
                                                ftp.ftpTruncateDirectory();
                                                break;
                                            default:
                                                ftp.ftpAuth();
                                                ftp.ftpListFiles();
                                                break;
                                        }
                                        break;

                                    case "ICMP":
                                        //          IcmpClient icmp = new IcmpClient(session.icmp_Servidor, session.icmp_Quantidade);
                                        //            icmp.PerformServerConnection();
                                        break;

                                    case "NTP":
                                        NtpClient ntp = new NtpClient(session.ntp_Servidor);
                                        ntp.PerformServerConnection();
                                        break;

                                    case "SMB":
                                        SmbClient smb = new SmbClient(session.smb_Usuario, session.smb_Dominio, session.smb_Servidor, new String(session.smb_Password), session.smb_NovoValor, session.smb_Conteudo, session.smb_Diretorio, session.smb_ValorAnterior, session.smb_Protocolo);
                                        switch (session.smb_Operacao) {
                                            case 0:
                                                smb.smbAuth();
                                                break;
                                            case 1:
                                                smb.writeToFile();
                                                break;
                                            case 2:
                                                smb.readTextFileFromDirectory();
                                                break;
                                            case 3:
                                                smb.writeToFile();
                                                smb.readTextFileFromDirectory();
                                                break;
                                            case 4:
                                                smb.renameFileInDirectory();
                                                break;
                                            case 5:
                                                smb.smbListDir();
                                                break;
                                            case 6:
                                                smb.truncateDirectory();
                                                break;
                                            default:
                                                smb.smbListDir();
                                                break;
                                        }
                                        break;

                                    case "SMTP":
                                        SmtpClient smtp = new SmtpClient();
                                        smtp.PerformServerConnection(
                                                session.smtp_Servidor,
                                                session.smtp_Porta,
                                                session.smtp_IsAutenticacao ? "true" : "false",
                                                session.smtp_Protocolo,
                                                session.smtp_IsStartTls ? "true" : "false",
                                                session.smtp_Remetente,
                                                new String(session.smtp_Password),
                                                session.smtp_Destinatario,
                                                session.smtp_Titulo,
                                                session.smtp_CorpoEmail
                                        );
                                        break;

                                    case "SOCKET":
                                        SocketClient socket = new SocketClient();
                                        socket.PerformServerConnection(session.socket_Servidor, session.socket_Porta);
                                        break;

                                    case "SSH":
                                        SshClient ssh = new SshClient(session.ssh_Servidor, session.ssh_Porta, session.ssh_Instrucao, session.ssh_Usuario, new String(session.ssh_Senha));
                                        ssh.PerformServerConnection();
                                        break;

                                    case "TELNET":
                                        Telnet_Client telnet = new Telnet_Client(session.telnet_Servidor, session.telnet_Porta, session.telnet_Instrucao);
                                        telnet.PerformServerConnection();
                                        break;

                                    default:
                                        System.out.println("Workspace desconhecido: " + monitoring.workspace);
                                        break;
                                }
                            } catch (Exception ex) {
                                System.out.println("Erro ao processar sessão para workspace " + monitoring.workspace + ": " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        }
                    }

                    Thread.sleep(intervalInSeconds * 1000L);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrompida. " + e.getMessage());
                    break;
                }
            }
        });

        workerThread.setDaemon(true);
        workerThread.start();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Programa principal interrompido. " + e.getMessage());
        }
    }
}
