/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UserConfig;

import Entities.Usuario;
import Enum.LogLevel;

/**
 *
 * @author Mauros
 */
public class UserProperties {

    private static LogLevel logLevel = LogLevel.DEBUG;
    private static Usuario usuarioLogado;

    public static LogLevel getLogLevel() {
        return logLevel;
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void setUsuarioLogado(Usuario user) {
        UserProperties.usuarioLogado = user;
    }

}
