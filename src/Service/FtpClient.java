/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import Entities.LogOccurrenceModule;
import Enum.LogLevel;
import java.io.*;
import java.util.*;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

/**
 *
 * @author 320167484
 */
public class FtpClient {

    private final String usr;
    private final String spwd;
    private String dirRemoto;
    private final String dirLocal;
    private final String shost;
    private final int port;
    private final List<LogOccurrenceModule> logArray = new ArrayList<>();
    private FTPClient ftpClient = null;
    private final String protocolo;
    private boolean authenticated = false;

    public FtpClient(String usr, String shost, String spwd, String dirRemoto, String dirLocal, String porta, String protocolo) {
        this.usr = usr;
        this.spwd = spwd;
        this.dirRemoto = dirRemoto;
        this.dirLocal = dirLocal;
        this.shost = shost;
        this.port = Integer.parseInt(porta);
        this.protocolo = protocolo.toUpperCase();
    }

    public List<LogOccurrenceModule> ftpAuth() {
        addLog("Iniciando autenticação com o host destino.", LogLevel.INFO);

        if (protocolo.equals("FTPS (E)")) {
            ftpClient = new FTPSClient();
            addLog("Usando protocolo FTPS (Explícito).", LogLevel.INFO);
        } else if (protocolo.equals("FTPS (I)")) {
            ftpClient = new FTPSClient(true);
            addLog("Usando protocolo FTPS (Implícito).", LogLevel.INFO);
        } else {
            ftpClient = new FTPClient();
            addLog("Usando protocolo FTP.", LogLevel.INFO);
        }

        ftpClient.addProtocolCommandListener(new ProtocolCommandListener() {
            @Override
            public void protocolCommandSent(ProtocolCommandEvent event) {
                addLog("Comando enviado: " + event.getMessage().trim(), LogLevel.FINE);
            }

            @Override
            public void protocolReplyReceived(ProtocolCommandEvent event) {
                addLog("Resposta recebida: " + event.getMessage().trim(), LogLevel.INFO);
            }
        });

        try {
            ftpClient.connect(shost, port);
            int replyCode = ftpClient.getReplyCode();
            addLog("Conectado. Código de resposta: " + replyCode, LogLevel.FINE);

            if (!FTPReply.isPositiveCompletion(replyCode)) {
                addLog("Conexão rejeitada. Código: " + replyCode, LogLevel.ERROR);
                ftpClient.disconnect();
                authenticated = false;
                return logArray;
            }

            authenticated = ftpClient.login(usr, spwd);
            addLog("Código de resposta após login: " + ftpClient.getReplyCode(), LogLevel.INFO);
            addLog("Mensagem de resposta: " + ftpClient.getReplyString().trim(), LogLevel.INFO);

            if (authenticated) {
                addLog("Autenticado com sucesso no servidor destino.", LogLevel.FINE);
            } else {
                addLog("Não foi possível se autenticar no servidor destino.", LogLevel.ERROR);
            }

        } catch (IOException ex) {
            addLog("Erro na conexão ou autenticação: " + ex.getMessage(), LogLevel.ERROR);
            authenticated = false;
        }

        return logArray;
    }

    public List<LogOccurrenceModule> ftpUpload() {
        try (InputStream input = new FileInputStream(this.dirLocal)) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            boolean success = ftpClient.storeFile(this.dirRemoto, input);

            if (success) {
                addLog("Arquivo enviado com sucesso: " + this.dirRemoto, LogLevel.INFO);
            } else {
                addLog("Falha ao enviar arquivo: " + this.dirRemoto, LogLevel.ERROR);
            }
        } catch (IOException ex) {
            addLog("Erro durante upload: " + ex.getMessage(), LogLevel.ERROR);
        }

        return logArray;
    }

    public List<LogOccurrenceModule> ftpDownload() {
        try (OutputStream output = new FileOutputStream(this.dirLocal)) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            boolean success = ftpClient.retrieveFile(this.dirRemoto, output);

            if (success) {
                addLog("Arquivo baixado com sucesso: " + this.dirRemoto, LogLevel.INFO);
            } else {
                addLog("Falha ao baixar arquivo: " + this.dirRemoto, LogLevel.ERROR);
            }
        } catch (IOException ex) {
            addLog("Erro durante download: " + ex.getMessage(), LogLevel.ERROR);
        }

        return logArray;
    }

    public List<LogOccurrenceModule> ftpListFiles() {
        if (ftpClient == null || !ftpClient.isConnected() || !authenticated) {
            addLog("FTPClient não está conectado ou não autenticado.", LogLevel.ERROR);
            return logArray;
        }

        try {
            FTPFile[] files = ftpClient.listFiles(this.dirRemoto);
            for (FTPFile file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    addLog(fileName, LogLevel.INFO);
                    addLog("Arquivo encontrado: " + fileName, LogLevel.FINE);
                }
            }
        } catch (IOException ex) {
            addLog("Erro ao listar arquivos: " + ex.getMessage(), LogLevel.ERROR);
        }

        return logArray;
    }

    public List<LogOccurrenceModule> ftpTruncateDirectory() {
        try {
            truncateDirectoryRecursive(this.dirRemoto);
        } catch (IOException ex) {
            addLog("Erro ao truncar diretório: " + ex.getMessage(), LogLevel.ERROR);
        }
        return logArray;
    }

    private void truncateDirectoryRecursive(String remoteDir) throws IOException {
        FTPFile[] files = ftpClient.listFiles(remoteDir);

        for (FTPFile file : files) {
            String path = remoteDir + "/" + file.getName();
            if (file.isDirectory()) {
                // Recursivamente limpa a subpasta
                truncateDirectoryRecursive(path);
                boolean removed = ftpClient.removeDirectory(path);
                if (!removed) {
                    addLog("Erro ao remover diretório: " + path, LogLevel.ERROR);
                } else {
                    addLog("Diretório removido: " + path, LogLevel.INFO);
                }
            } else {
                boolean deleted = ftpClient.deleteFile(path);
                if (!deleted) {
                    addLog("Erro ao excluir arquivo: " + path, LogLevel.ERROR);
                } else {
                    addLog("Arquivo excluído: " + path, LogLevel.INFO);
                }
            }
        }
    }

    private void addLog(String message, LogLevel level) {
        logArray.add(new LogOccurrenceModule(message, level, this.shost));
    }

    public List<LogOccurrenceModule> getLogArray() {
        return logArray;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
