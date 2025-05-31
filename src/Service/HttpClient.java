/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import Entities.LogOccurrenceModule;
import Enum.LogLevel;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author 320167484
 */
public class HttpClient {

    private final StringWriter sw = new StringWriter();
    private final PrintWriter pw = new PrintWriter(sw);
    private String protocolo;
    private String endpoint;
    private String parametros;
    private final ArrayList<LogOccurrenceModule> LogArray = new ArrayList<>();

    // HttpClient http = new HttpClient(protocoloCHB.getSelectedItem().toString(), endPointTF.getText(), parametrosTF.getText());
    public HttpClient(String protocolo, String endpoint, String parametros) {
        this.protocolo = protocolo;
        this.endpoint = endpoint;
        this.parametros = parametros;
    }

    public List<LogOccurrenceModule> sendGetRequest() {
        StringBuilder response = new StringBuilder();

        String endpointUrl = protocolo + "://" + endpoint;

        try {
            URL url = new URL(endpointUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 segundos de timeout
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            addToArray("Código de resposta GET: " + responseCode, LogLevel.INFO);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8")
                );
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                addToArray("Corpo da resposta (HTML): " + response.toString(), LogLevel.DEBUG);
            } else {
                addToArray("Falha no GET. Código HTTP: " + responseCode, LogLevel.INFO);

            }

        } catch (Exception e) {
            e.printStackTrace(pw);
            this.addToArray(sw.toString(), LogLevel.ERROR);

        }

        return this.getLogArray();
    }

    // Método PUT
    public List<LogOccurrenceModule> sendPutRequest(Map<String, String> parameters) {
        StringBuilder response = new StringBuilder();
        String endpointUrl = protocolo + "://" + endpoint;
        try {
            URL url = new URL(endpointUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // Configura o tipo de conteúdo (pode ser alterado se quiser enviar JSON, etc)
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            // Construir o corpo da requisição com os parâmetros
            StringBuilder params = new StringBuilder();
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (params.length() != 0) {
                    params.append("&");
                }
                params.append(entry.getKey()).append("=").append(entry.getValue());
            }

            // Enviar os parâmetros
            try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                out.writeBytes(params.toString());
                out.flush();
            }

            int responseCode = connection.getResponseCode();
            addToArray("Código de resposta PUT: " + responseCode, LogLevel.INFO);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8")
                );
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } else {
                addToArray("Falha no PUT. Código HTTP: " + responseCode, LogLevel.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace(pw);
            this.addToArray(sw.toString(), LogLevel.ERROR);

        }

        return this.getLogArray();
    }

    private void addToArray(String input, LogLevel level) {

        LogOccurrenceModule log = new LogOccurrenceModule(input, level, this.endpoint);
        this.LogArray.add(log);
    }

    public ArrayList<LogOccurrenceModule> getLogArray() {
        return LogArray;
    }

}
