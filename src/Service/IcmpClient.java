/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import Entities.LogOccurrenceModule;
import Enum.LogLevel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.shortpasta.icmp2.IcmpPingRequest;
import org.shortpasta.icmp2.IcmpPingResponse;
import org.shortpasta.icmp2.IcmpPingUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author 320167484
 */
public class IcmpClient {

    private final StringWriter sw = new StringWriter();
    private final PrintWriter pw = new PrintWriter(sw);
    private final ArrayList<LogOccurrenceModule> LogArray = new ArrayList<>();
    private final String host;
    private String infoRtt;
    private String hostAddress;
    private int packSize;
    private int valueRtt;
    private int valueTtl;
    private HashMap<String, Object> map;
    private LogLevel level;
    private final int qtd;

    public IcmpClient(String host, String qtd) {
        this.host = host;
        this.qtd = Integer.parseInt(qtd);
    }

    public ArrayList<LogOccurrenceModule> PerformServerConnection() {

        int cont = 0;
        while (cont < qtd) {

            String output = "";
            try {
                InetAddress inet = InetAddress.getByName(host);
                setHostAddress(inet.getHostAddress());

                IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
                request.setHost(host);

                IcmpPingResponse response = IcmpPingUtil.executePingRequest(request);

                output += "\nPing para: " + host + "\n";

                if (response.getSuccessFlag()) {
                    setValueRtt(response.getRtt());
                    setPackSize(response.getSize());
                    setValueTtl(response.getTtl());
                    verifyRtt();

                    output += "Resposta em: " + this.getValueRtt() + " ms\n";
                    output += this.getInfoRtt() + "\n";
                    output += "Endereço de IP Resolvido: " + this.getHostAddress() + "\n";
                    output += "Tamanho pacote de resposta recebido: " + this.getPackSize() + "\n";
                    output += "TTL: " + this.getValueTtl() + "\n";
                    
                    // Informações do endereço IP Alvo
                    this.getAddressInfos(this.getHostAddress());
                    
                    output += displayHostInfos();
                    
                } else {
                    output += "Falha no ping: " + response.getErrorMessage();
                }
            } catch (Exception e) {
                output += "Erro ao resolver host: " + e.getMessage();
                setLevel(LogLevel.SEVERE);
            }
            addToArray(output, level);
            cont++;
        }

        return getLogArray();
    }
    
    private String displayHostInfos(){
        StringBuilder str = new StringBuilder();
        str.append("Status: ").append(map.get("status")).append("\n");
        str.append("Pais Origem: ").append(map.get("country")).append("\n");
        str.append("Código do Pais: ").append(map.get("countryCode")).append("\n");
        str.append("Região: ").append(map.get("region")).append("\n");
        str.append("Nome da Região: ").append(map.get("regionName")).append("\n");
        str.append("Cidade: ").append(map.get("city")).append("\n");
        str.append("Código Postal: ").append(map.get("zip")).append("\n");
        str.append("Latitude: ").append(map.get("lat")).append("\n");
        str.append("Longitude: ").append(map.get("lon")).append("\n");
        str.append("Timezone: ").append(map.get("timezone")).append("\n");
        str.append("Provedor de Internet: ").append(map.get("isp")).append("\n");
        str.append("Organização: ").append(map.get("org")).append("\n");
        str.append("Sistema Autonomo: ").append(map.get("as")).append("\n");
        str.append("Endereço de IP: ").append(map.get("query")).append("\n");
        str.append("Localização Aproximada:\nhttps://www.google.com/maps?q=").append(map.get("lat")).append(",").append(map.get("lon"));
        return str.toString();
    }
    
    private void verifyRtt() {

        if (this.getValueRtt() <= 30) {
            setInfoRtt("Latência Excelente. Comum em redes locais (LAN), conexão de alta qualidade.");
            setLevel(level.FINE);
        } else if (this.getValueRtt() <= 100) {
            setInfoRtt("Latência moderada. Pode gerar pequenos atrasos perceptíveis.");
            setLevel(level.WARNING);
        } else if (this.getValueRtt() <= 200) {
            setInfoRtt("Latência Alta. Atrasos são frequentemente perceptíveis.");
            setLevel(level.WARNING);
        } else if (this.getValueRtt() <= 500) {
            setInfoRtt("Latência Muito Alta. Lentidão e travamentos perceptíveis devido a rede.");
            setLevel(level.SEVERE);
        } else {
            setInfoRtt("Latência Extrema. Quase inutilizável.");
            setLevel(level.SEVERE);
        }
    }

    private void getAddressInfos(String host) {
        String url = "http://ip-api.com/json/" + host;

        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            this.JsonToMap(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void JsonToMap(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.map = mapper.readValue(json, HashMap.class);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(IcmpClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public int getPackSize() {
        return packSize;
    }

    public void setPackSize(int packSize) {
        this.packSize = packSize;
    }

    public int getValueRtt() {
        return valueRtt;
    }

    public void setValueRtt(int valueRtt) {
        this.valueRtt = valueRtt;
    }

    public int getValueTtl() {
        return valueTtl;
    }

    public void setValueTtl(int valueTtl) {
        this.valueTtl = valueTtl;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public String getInfoRtt() {
        return infoRtt;
    }

    public void setInfoRtt(String infoRtt) {
        this.infoRtt = infoRtt;
    }

    private void addToArray(String input, LogLevel level) {

        LogOccurrenceModule log = new LogOccurrenceModule(input, level);
        this.LogArray.add(log);
    }

    public ArrayList<LogOccurrenceModule> getLogArray() {
        return LogArray;
    }

}
