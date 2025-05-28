/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import Entities.LogOccurrenceModule;
import Enum.LogLevel;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.commons.net.telnet.TelnetClient;
import com.jcraft.jsch.*;

import java.io.InputStream;

/**
 *
 * @author 320167484
 */
public class SshClient {

    private final StringWriter sw = new StringWriter();
    private final PrintWriter pw = new PrintWriter(sw);
    private final ArrayList<LogOccurrenceModule> LogArray = new ArrayList<>();
    private String host;
    private int port;
    private String comando;
    private String usuario;
    private String senha;

    public SshClient(String host, String port, String comando, String usuario, String senha) {
        this.host = host;
        this.port = Integer.parseInt(port);
        this.comando = comando;
        this.usuario = usuario;
        this.senha = senha;
    }

    public ArrayList<LogOccurrenceModule> PerformServerConnection() {

        addToArray("\n\n***Instrução encaminhada ao servidor:***\n>" + comando + "\n", LogLevel.INFO);
        Session session = null;
        ChannelExec channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(usuario, host, port);
            session.setPassword(senha);

            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(comando);

            channel.setErrStream(System.err);
            InputStream in = channel.getInputStream();

            channel.connect();
            String retorno = "\n\n***Retorno do servidor:***\n\n";
            String exitStatus = "";
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    retorno += new String(tmp, 0, i);
                }
                if (channel.isClosed()) {
                    exitStatus = "\n\nExit status: " + channel.getExitStatus() + "\n";
                    break;
                }
                Thread.sleep(1000);
            }
            addToArray(retorno, LogLevel.INFO);
            addToArray(exitStatus, LogLevel.INFO);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

        return getLogArray();
    }

    private void addToArray(String input, LogLevel level) {

        LogOccurrenceModule log = new LogOccurrenceModule(input, level);
        this.LogArray.add(log);
    }

    public ArrayList<LogOccurrenceModule> getLogArray() {
        return LogArray;
    }

}
