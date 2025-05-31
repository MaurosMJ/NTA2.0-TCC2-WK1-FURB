/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entities;

import Enum.LogLevel;
import Enum.Module;

/**
 *
 * @author Mauros
 */
public class LogOcurrenceMonitoring {

    private String host;
    private LogLevel level;
    private Module modulo;
    private String log;
    private double icmp;
    private String occurrence;
    private String notificacao;

    public LogOcurrenceMonitoring(String host, LogLevel level, Module modulo, String log, double icmp, String occurrence, String notificacao) {
        this.host = host;
        this.level = level;
        this.modulo = modulo;
        this.log = log;
        this.icmp = icmp;
        this.occurrence = occurrence;
        this.notificacao = notificacao;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public Module getModulo() {
        return modulo;
    }

    public void setModulo(Module modulo) {
        this.modulo = modulo;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public double getIcmp() {
        return icmp;
    }

    public void setIcmp(double icmp) {
        this.icmp = icmp;
    }

    public String getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(String Occurrence) {
        this.occurrence = Occurrence;
    }

    public String getNotificacao() {
        return notificacao;
    }

    public void setNotificacao(String notificacao) {
        this.notificacao = notificacao;
    }

}
