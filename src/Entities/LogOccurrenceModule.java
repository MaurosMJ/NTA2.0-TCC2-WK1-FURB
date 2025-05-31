/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entities;

import Enum.LogLevel;
import Utils.HostConfig;
import java.util.Date;

/**
 *
 * @author 320167484
 */
public class LogOccurrenceModule {
    
    private String data;
    private String host;
    private String user;
    private String system;
    private LogLevel level;
    private Date Occurrence;
    
    public LogOccurrenceModule (String data, LogLevel level, String host){
        this.setData(data);
        this.setSeverity(level);
        this.setOccurrence(HostConfig.obterDataAtual());
        this.setHost(host);
        this.setSystem(HostConfig.obterSistemaOperacional());
        this.setUser(HostConfig.obterNomeUsuario());
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public LogLevel getSeverity() {
        return level;
    }

    public void setSeverity(LogLevel level) {
        this.level = level;
    }

    public Date getOccurrence() {
        return Occurrence;
    }

    public void setOccurrence(Date Occurrence) {
        this.Occurrence = Occurrence;
    }
    
    
}
