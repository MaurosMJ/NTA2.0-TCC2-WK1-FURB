/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import Entities.LogOccurrenceModule;
import Enum.LogLevel;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

/**
 *
 * @author 320167484
 */
public class NtpClient {

    private final ArrayList<LogOccurrenceModule> LogArray = new ArrayList<>();
    private final String host;

    public NtpClient(String host) {
        this.host = host;
    }

    public ArrayList<LogOccurrenceModule> PerformServerConnection() {
        try {
            NTPUDPClient client = new NTPUDPClient();
            client.setDefaultTimeout(3000);
            InetAddress hostAddr = InetAddress.getByName(host);
            TimeInfo info = client.getTime(hostAddr);
            info.computeDetails();

            long currentTime = info.getMessage().getTransmitTimeStamp().getTime();
            Date time = new Date(currentTime);
            addToArray("Horário do servidor NTP: " + time , LogLevel.FINE);
        } catch (Exception e) {
            addToArray("Erro ao consultar servidor NTP: " + e.getMessage(), LogLevel.ERROR);
        }
        return getLogArray();
    }

    private void addToArray(String input, LogLevel level) {

        LogOccurrenceModule log = new LogOccurrenceModule(input, level, this.host);
        this.LogArray.add(log);
    }

    public ArrayList<LogOccurrenceModule> getLogArray() {
        return LogArray;
    }

}
