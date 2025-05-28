/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import Entities.LogOccurrenceModule;
import Enum.LogLevel;
import UserConfig.UserProperties;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *
 * @author Mauros
 */
public class HostConfig {

    private static DateTimeFormatter formatterDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat formatterSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String obterSistemaOperacional() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return "Windows";
        } else if (os.contains("mac")) {
            return "Mac";
        } else if (os.contains("nux") || os.contains("nix")) {
            return "Linux";
        } else {
            return "Desconhecido";
        }
    }

    public static String obterNomeUsuario() {
        return System.getProperty("user.name");
    }

    public static String obterNomeMaquina() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (UnknownHostException e) {
            return "Desconhecido";
        }
    }

    public static Date obterDataAtual() {
        return new Date();
    }

    public static String getDataHoraAtual() {
        return LocalDateTime.now().format(formatterDTF);
    }

    public static String getLogFormat(LogLevel nivel, String mensagem) {

        if (permitirLogGeracao(nivel)) {
            return String.format("[%s] [%s] [%s] [%s] [%s] %s",
                    getDataHoraAtual(),
                    nivel,
                    obterNomeMaquina(),
                    obterNomeUsuario(),
                    obterSistemaOperacional(),
                    mensagem);
        } else {
            return "";
        }
    }

    public static String getLogFormatFromLogOccurrence(LogOccurrenceModule log) {

        if (permitirLogGeracao(log.getSeverity())) {
            return String.format("[%s] [%s] [%s] [%s] [%s] %s",
                    formatterSDF.format(log.getOccurrence()),
                    log.getSeverity(),
                    log.getHost(),
                    log.getUser(),
                    log.getSystem(),
                    log.getData());
        } else {
            return "";
        }
    }

    public static boolean permitirLogGeracao(LogLevel nivelGerado) {
        return nivelGerado.getPrioridade() >= UserProperties.getLogLevel().getPrioridade();
    }

}
