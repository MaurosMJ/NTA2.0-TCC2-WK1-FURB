/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Persistence.Logs;

import Enum.LogLevel;
import java.util.List;

/**
 *
 * @author Mauros
 */
public class LogPersistence {

    public String module;
    public List<SessionValues> session;

    public static class SessionValues {

        public String data;
        public String maquina;
        public LogLevel level;
        public String log;
        public double icmpRequest;
        public String notificacao;
    }
}
