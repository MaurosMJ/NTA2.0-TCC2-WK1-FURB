/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Enum;

/**
 *
 * @author Mauros
 */
public enum LogLevel {

    DEBUG(1),
    FINE(2),
    INFO(3),
    WARNING(4),
    ERROR(5),
    SEVERE(6);

    private final int prioridade;

    LogLevel(int prioridade) {
        this.prioridade = prioridade;
    }

    public int getPrioridade() {
        return prioridade;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
