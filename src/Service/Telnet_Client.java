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

/**
 *
 * @author 320167484
 */
public class Telnet_Client {

    private final StringWriter sw = new StringWriter();
    private final PrintWriter pw = new PrintWriter(sw);
    private final ArrayList<LogOccurrenceModule> LogArray = new ArrayList<>();
    private String host;
    private int port;
    String instrucao;

    public Telnet_Client(String host, String port, String instucao) {
        this.host = host;
        this.port = Integer.parseInt(port);
        this.instrucao = instucao;
    }

    public ArrayList<LogOccurrenceModule> PerformServerConnection() {
        TelnetClient telnet = new TelnetClient();

        try {
            telnet.connect(host, port);
            addToArray("Conectado via Telnet!", LogLevel.INFO);
            InputStream in = telnet.getInputStream();
            OutputStream out = telnet.getOutputStream();
            PrintStream ps = new PrintStream(out);

            ps.println(this.instrucao);
            ps.flush();

            // Ler resposta
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
                if (!reader.ready()) {
                    break;
                }
            }
            addToArray("Resposta:\n" + response.toString(), LogLevel.INFO);

            telnet.disconnect();
        } catch (Exception e) {
            addToArray("Erro: " + e.getMessage(), LogLevel.ERROR);
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
