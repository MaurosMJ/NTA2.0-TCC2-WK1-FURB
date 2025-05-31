/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntaworker1;

import Entities.LogOccurrenceModule;
import Entities.LogOcurrenceMonitoring;
import Enum.Module;
import Persistence.Worker1.Worker1Persistence;
import Persistence.JsonPersistence;
import Persistence.Logs.LogPersistence;
import Service.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mauros
 */
public class NtaWorker1 {

    private static int intervalInSeconds = 10;
    private final List<Worker1Persistence> MonitoringArray = new ArrayList<>();
    private ArrayList<LogOcurrenceMonitoring> LogArray = new ArrayList<>();

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

    private String obterDataAtual() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    public void AddLogJsonMonitoring(LogOcurrenceMonitoring log) {

        String nomeArquivo = "LogNTA.json";

        // Cria uma nova entrada de sessão
        LogPersistence.SessionValues novaSessao = new LogPersistence.SessionValues();
        novaSessao.data = obterDataAtual();
        novaSessao.icmpRequest = log.getIcmp();
        novaSessao.level = log.getLevel();
        novaSessao.maquina = log.getHost();
        novaSessao.log = log.getLog();

        JsonPersistence.adicionarEntradaAoLog(
                nomeArquivo,
                log.getModulo().toString(),
                novaSessao
        );

    }

    private void addToArray(List<LogOccurrenceModule> list, Module module, int icmp) {

        for (LogOccurrenceModule logs : list) {
            LogOcurrenceMonitoring log = new LogOcurrenceMonitoring(logs.getHost(), logs.getSeverity(), module, logs.getData(), icmp, obterDataAtual(), "N");
            AddLogJsonMonitoring(log);
            this.LogArray.add(log);
        }

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
        Locale.setDefault(new Locale("pt", "BR"));
        NtaWorker1 instance = new NtaWorker1();

        Thread workerThread = new Thread(() -> {
            while (true) {
                try {
                    System.out.println("Executando tarefa...");

                    for (Worker1Persistence monitoring : instance.MonitoringArray) {
                        for (Worker1Persistence.SessionValues session : monitoring.session) {
                            try {
                                IcmpClient icmp;
                                switch (monitoring.workspace.toUpperCase()) {
                                    case "HTTP":
                                        HttpClient http = new HttpClient(session.http_Protocolo, session.http_Endpoint, session.http_Parametros);
                                        icmp = new IcmpClient(session.http_Endpoint, 1);
                                        icmp.PerformServerConnection();
                                        if (session.http_Operacao == 0) {
                                            instance.addToArray(http.sendGetRequest(), Module.HTTP, icmp.getValueRtt());
                                            break;
                                        }
                                        Map<String, String> parametrosMap;
                                        parametrosMap = instance.parseParametersString(session.http_Parametros);
                                        instance.addToArray(http.sendPutRequest(parametrosMap), Module.HTTP, icmp.getValueRtt());
                                        break;

                                    case "DNS":
                                        icmp = new IcmpClient(session.dns_Servidor, 1);
                                        icmp.PerformServerConnection();
                                        DnsClient dns = new DnsClient(session.dns_Servidor, session.dns_Dominio, session.dns_Tipo, session.dns_Classe);
                                        instance.addToArray(dns.PerformServerConnection(), Module.DNS, icmp.getValueRtt());
                                        break;

                                    case "FTP":
                                        icmp = new IcmpClient(session.ftp_Servidor, 1);
                                        icmp.PerformServerConnection();
                                        FtpClient ftp = new FtpClient(session.ftp_Usuario, session.ftp_Servidor, new String(session.ftp_Password), session.ftp_DiretorioRemoto, session.ftp_DiretorioAtual, session.ftp_Porta, session.ftp_Protocolo);
                                        switch (session.ftp_Operacao) {
                                            case 0:
                                                instance.addToArray(ftp.ftpAuth(), Module.FTP, icmp.getValueRtt());
                                                break;
                                            case 1:
                                                instance.addToArray(ftp.ftpAuth(), Module.FTP, icmp.getValueRtt());
                                                instance.addToArray(ftp.ftpDownload(), Module.FTP, icmp.getValueRtt());
                                                break;
                                            case 2:
                                                instance.addToArray(ftp.ftpAuth(), Module.FTP, icmp.getValueRtt());
                                                instance.addToArray(ftp.ftpUpload(), Module.FTP, icmp.getValueRtt());
                                                ftp.ftpUpload();
                                                break;
                                            case 3:
                                                instance.addToArray(ftp.ftpAuth(), Module.FTP, icmp.getValueRtt());
                                                instance.addToArray(ftp.ftpListFiles(), Module.FTP, icmp.getValueRtt());
                                                ftp.ftpListFiles();
                                                break;
                                            case 4:
                                                instance.addToArray(ftp.ftpAuth(), Module.FTP, icmp.getValueRtt());
                                                instance.addToArray(ftp.ftpTruncateDirectory(), Module.FTP, icmp.getValueRtt());
                                                ftp.ftpTruncateDirectory();
                                                break;
                                            default:
                                                instance.addToArray(ftp.ftpAuth(), Module.FTP, icmp.getValueRtt());
                                                instance.addToArray(ftp.ftpTruncateDirectory(), Module.FTP, icmp.getValueRtt());
                                                break;
                                        }
                                        break;

                                    case "ICMP":
                                        icmp = new IcmpClient(session.icmp_Servidor, Integer.parseInt(session.icmp_Quantidade));
                                        instance.addToArray(icmp.PerformServerConnection(), Module.ICMP, icmp.getValueRtt());
                                        break;

                                    case "NTP":
                                        icmp = new IcmpClient(session.ntp_Servidor, 1);
                                        icmp.PerformServerConnection();
                                        NtpClient ntp = new NtpClient(session.ntp_Servidor);
                                        instance.addToArray(ntp.PerformServerConnection(), Module.NTP, icmp.getValueRtt());
                                        break;

                                    case "SMB":
                                        icmp = new IcmpClient(session.smb_Servidor, 1);
                                        icmp.PerformServerConnection();
                                        SmbClient smb = new SmbClient(session.smb_Usuario, session.smb_Dominio, session.smb_Servidor, new String(session.smb_Password), session.smb_NovoValor, session.smb_Conteudo, session.smb_Diretorio, session.smb_ValorAnterior, session.smb_Protocolo);
                                        switch (session.smb_Operacao) {
                                            case 0:
                                                instance.addToArray(smb.smbAuth(), Module.SMB, icmp.getValueRtt());
                                                break;
                                            case 1:
                                                instance.addToArray(smb.writeToFile(), Module.SMB, icmp.getValueRtt());
                                                break;
                                            case 2:
                                                instance.addToArray(smb.readTextFileFromDirectory(), Module.SMB, icmp.getValueRtt());
                                                break;
                                            case 3:
                                                instance.addToArray(smb.writeToFile(), Module.SMB, icmp.getValueRtt());
                                                instance.addToArray(smb.readTextFileFromDirectory(), Module.SMB, icmp.getValueRtt());
                                                break;
                                            case 4:
                                                instance.addToArray(smb.renameFileInDirectory(), Module.SMB, icmp.getValueRtt());
                                                break;
                                            case 5:
                                                instance.addToArray(smb.smbListDir(), Module.SMB, icmp.getValueRtt());
                                                break;
                                            case 6:
                                                instance.addToArray(smb.truncateDirectory(), Module.SMB, icmp.getValueRtt());
                                                break;
                                            default:
                                                instance.addToArray(smb.smbListDir(), Module.SMB, icmp.getValueRtt());
                                                break;
                                        }
                                        break;

                                    case "SMTP":
                                        icmp = new IcmpClient(session.smtp_Servidor, 1);
                                        icmp.PerformServerConnection();
                                        SmtpClient smtp = new SmtpClient();
                                        instance.addToArray(
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
                                                ), Module.SMTP, icmp.getValueRtt());
                                        break;

                                    case "SOCKET":
                                        icmp = new IcmpClient(session.socket_Servidor, 1);
                                        icmp.PerformServerConnection();
                                        SocketClient socket = new SocketClient();
                                        instance.addToArray(socket.PerformServerConnection(session.socket_Servidor, session.socket_Porta), Module.SOCKET, icmp.getValueRtt());
                                        break;

                                    case "SSH":
                                        icmp = new IcmpClient(session.ssh_Servidor, 1);
                                        icmp.PerformServerConnection();
                                        SshClient ssh = new SshClient(session.ssh_Servidor, session.ssh_Porta, session.ssh_Instrucao, session.ssh_Usuario, new String(session.ssh_Senha));
                                        instance.addToArray(ssh.PerformServerConnection(), Module.SSH, icmp.getValueRtt());
                                        break;

                                    case "TELNET":
                                        icmp = new IcmpClient(session.telnet_Servidor, 1);
                                        icmp.PerformServerConnection();
                                        Telnet_Client telnet = new Telnet_Client(session.telnet_Servidor, session.telnet_Porta, session.telnet_Instrucao);
                                        instance.addToArray(telnet.PerformServerConnection(), Module.TELNET, icmp.getValueRtt());
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
