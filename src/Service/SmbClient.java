/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import Entities.LogOccurrenceModule;
import Enum.LogLevel;
import jcifs.smb.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

/**
 *
 * @author 320167484
 */
public class SmbClient {

    private final String usr;
    private final String dmn;
    private final String spwd;
    private String fileName;
    private final String fileContent;
    private final String antigoValor;
    private final String shost;
    private NtlmPasswordAuthentication auth;
    private final List<LogOccurrenceModule> logArray = new ArrayList<>();
    private final String protocolo;

    public SmbClient(String usr, String dmn, String shost, String spwd, String fileName, String fileContent, String dir, String antigoValor, String protocolo) {
        this.usr = usr;
        this.dmn = dmn;
        this.spwd = spwd;
        this.fileName = fileName;
        this.antigoValor = antigoValor;
        this.fileContent = fileContent.isEmpty() ? "Arquivo encaminhado ao servidor!" : fileContent;
        this.shost = formatHost(shost, dir);
        this.protocolo = protocolo;
    }

    private String formatHost(String host, String dir) {
        if (!host.startsWith("\\\\")) {
            host = "\\\\" + host;
        }
        if (!dir.startsWith("\\")) {
            dir = "\\" + dir;
        }
        return host + dir;
    }

    private void configurarProtocoloSMB() {
        switch (protocolo.toUpperCase()) {
            case "SMB2.0":
                System.setProperty("jcifs.smb.client.minVersion", "SMB2");
                System.setProperty("jcifs.smb.client.maxVersion", "SMB2");
                break;
            case "SMB3.0":
                System.setProperty("jcifs.smb.client.minVersion", "SMB3");
                System.setProperty("jcifs.smb.client.maxVersion", "SMB3");
                break;
            default:
                System.setProperty("jcifs.smb.client.minVersion", "SMB1");
                System.setProperty("jcifs.smb.client.maxVersion", "SMB1");
                break;
        }
    }

    public List<LogOccurrenceModule> smbAuth() {
        System.out.println("Iniciando autenticação com o host destino.");
        configurarProtocoloSMB();
        try {
            this.auth = new NtlmPasswordAuthentication(dmn, usr, spwd);
            SmbFile file = new SmbFile(getSmbUrl(), auth);
            file.connect(); // Força autenticação

            addLog("Autenticado no servidor.", LogLevel.INFO);
        } catch (Exception e) {
            logException("Erro ao autenticar no servidor.", e);
        }
        return logArray;
    }

    public List<LogOccurrenceModule> writeToFile() {
        if (!authenticate()) {
            return logArray;
        }

        this.fileName = fileName.isEmpty() ? "Nta" : fileName;
        String filePath = getSmbUrl() + "/" + generateFileName();
        this.fileName = "";
        System.out.println("Caminho SMB final: " + filePath);

        try (SmbFileOutputStream outputStream = new SmbFileOutputStream(new SmbFile(filePath, auth))) {
            outputStream.write(fileContent.getBytes());
            addLog("[WRITE = OK] Arquivo enviado com sucesso ao servidor!", LogLevel.INFO);
        } catch (IOException e) {
            logException("Erro ao escrever arquivo no servidor SMB.", e);
        }
        return logArray;
    }

    public List<LogOccurrenceModule> renameFileInDirectory() {
        System.out.println("Iniciando renomeação de arquivo no diretório.");

        this.smbAuth(); // Autenticação SMB

        String smbPath = shost.replace("\\", "/");
        smbPath = "smb:" + smbPath + "/";

        try {
            SmbFile directory = new SmbFile(smbPath, auth);

            if (directory.exists() && directory.isDirectory()) {
                SmbFile[] files = directory.listFiles();

                if (files == null || files.length == 0) {
                    addLog("Nenhum arquivo encontrado no diretório.", LogLevel.ERROR);
                    return getLogArray();
                }

                SmbFile sourceFile = null;
                for (SmbFile file : files) {
                    if (file.isFile() && file.getName().equalsIgnoreCase(antigoValor)) {
                        sourceFile = file;
                        break;
                    }
                }

                if (sourceFile == null) {
                    addLog("Arquivo a ser renomeado não encontrado: " + antigoValor, LogLevel.ERROR);
                    return getLogArray();
                }

                // Preparar novo caminho (lembrando que o caminho completo é necessário)
                String newFilePath = smbPath + fileName;
                SmbFile destinationFile = new SmbFile(newFilePath, auth);

                sourceFile.renameTo(destinationFile);

                addLog("Arquivo renomeado de '" + antigoValor + "' para '" + fileName + "'.", LogLevel.INFO);

            } else {
                addLog("Diretório não encontrado ou não é um diretório.", LogLevel.ERROR);
            }

        } catch (Exception e) {
            logException("Erro ao renomear o arquivo no diretório.", e);
        }

        return getLogArray();
    }

    public List<LogOccurrenceModule> smbListDir() {
        if (!authenticate()) {
            return getLogArray();
        }

        try {
            SmbFile directory = new SmbFile(getSmbUrl() + "/", auth);
            if (directory.exists() && directory.isDirectory()) {
                addLog("INICIANDO LISTAGEM:", LogLevel.INFO);
                for (SmbFile file : directory.listFiles()) {
                    addLog("Arquivo encontrado: " + file.getName(), LogLevel.INFO);
                }
            }
        } catch (IOException e) {
            logException("Erro ao listar diretórios no servidor SMB.", e);
        }
        return getLogArray();
    }

    public List<LogOccurrenceModule> truncateDirectory() {
        System.out.println("Iniciando truncamento do diretório.");

        smbAuth();

        String smbPath = "smb:" + shost.replace("\\", "/") + "/";

        try {
            SmbFile directory = new SmbFile(smbPath, auth);

            if (!directory.exists() || !directory.isDirectory()) {
                addLog("Diretório não encontrado ou não é um diretório.", LogLevel.ERROR);
                return getLogArray();
            }

            SmbFile[] files = directory.listFiles();
            for (SmbFile file : files) {
                if (file.isFile()) {
                    try {
                        file.delete();
                        addLog(String.format("Arquivo deletado: %s", file.getName()), LogLevel.INFO);
                    } catch (Exception e) {
                        addLog(String.format("Erro ao deletar arquivo: %s", file.getName()), LogLevel.ERROR);
                        addLog(getStackTraceAsString(e), LogLevel.DEBUG);
                    }
                } else if (file.isDirectory()) {
                    addLog(String.format("Ignorando subdiretório: %s", file.getName()), LogLevel.WARNING);
                }
            }

            addLog("Diretório truncado com sucesso.", LogLevel.INFO);

        } catch (Exception e) {
            logException("Erro ao truncar o diretório.", e);
        }

        return getLogArray();
    }

    public List<LogOccurrenceModule> readTextFileFromDirectory() {
        System.out.println("Iniciando leitura de arquivo de texto do diretório.");

        this.smbAuth(); // Autenticação SMB

        String smbPath = shost.replace("\\", "/");
        smbPath = "smb:" + smbPath + "/";

        String fileName = "";

        if (this.fileName.length() > 0) {
            fileName = this.fileName;

        }

        try {
            SmbFile directory = new SmbFile(smbPath, auth);

            if (directory.exists() && directory.isDirectory()) {
                SmbFile[] files = directory.listFiles();

                if (files == null || files.length == 0) {
                    addLog("Nenhum arquivo encontrado no diretório.", LogLevel.ERROR);
                    return this.getLogArray();
                }

                SmbFile targetFile = null;

                if (fileName != null && !fileName.trim().isEmpty()) {
                    // Usuário informou um nome de arquivo
                    for (SmbFile file : files) {
                        if (file.isFile() && file.getName().equalsIgnoreCase(fileName)) {
                            targetFile = file;
                            break;
                        }
                    }
                    if (targetFile == null) {
                        addLog("Arquivo informado não encontrado no diretório.", LogLevel.ERROR);
                        return this.getLogArray();
                    }
                } else {
                    // Usuário não informou nome -> pega o primeiro arquivo de texto
                    for (SmbFile file : files) {
                        if (file.isFile() && isTextFile(file.getName())) {
                            targetFile = file;
                            break;
                        }
                    }
                    if (targetFile == null) {
                        addLog("Nenhum arquivo de texto encontrado no diretório.", LogLevel.ERROR);
                        return this.getLogArray();
                    }
                }

                // Agora temos o arquivo alvo -> fazer a leitura
                try (InputStream is = targetFile.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append(System.lineSeparator());
                    }

                    addLog("Conteúdo lido com sucesso do arquivo: " + targetFile.getName(), LogLevel.INFO);
                    addLog("Conteúdo do arquivo: " + content.toString(), LogLevel.DEBUG); // Aqui adiciona o conteúdo lido
                }

            } else {
                addLog("Diretório não encontrado ou não é um diretório.", LogLevel.ERROR);
            }

        } catch (Exception e) {
            logException("Erro ao ler o arquivo do diretório.", e);
        }

        return this.getLogArray();
    }

    /**
     * Método auxiliar para verificar se é um arquivo de texto.
     */
    private boolean isTextFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".txt") || lowerName.endsWith(".log") || lowerName.endsWith(".csv")
                || lowerName.endsWith(".json") || lowerName.endsWith(".xml");
    }

    private boolean authenticate() {
        if (auth == null) {
            smbAuth();
        }
        return auth != null;
    }

    private String getSmbUrl() {
        return "smb:" + shost.replace("\\", "/");
    }

    private String generateFileName() {
        String timestamp = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ssss").format(new Date());
        return this.fileName + "-" + timestamp.replaceAll("[: ]", "") + ".txt";
    }

    private void logException(String message, Exception e) {
        Logger.getLogger(SmbClient.class.getName()).log(Level.SEVERE, message, e);
        addLog(message, LogLevel.ERROR);
        addLog(getStackTraceAsString(e), LogLevel.DEBUG);
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
        }
        return sw.toString();
    }

    private void addLog(String message, LogLevel level) {
        logArray.add(new LogOccurrenceModule(message, level, this.shost));
    }

    public List<LogOccurrenceModule> getLogArray() {
        return logArray;
    }
}
