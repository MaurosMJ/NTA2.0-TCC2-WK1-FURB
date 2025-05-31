package Service;

import Entities.LogOccurrenceModule;
import Enum.LogLevel;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DnsClient {

    private final ArrayList<LogOccurrenceModule> LogArray = new ArrayList<>();
    private final String host;
    private final String dominio;
    private final String tipo;
    private final String classe;

    public DnsClient(String host, String dominio, String tipo, String classe) {
        this.host = host;
        this.dominio = dominio;
        this.tipo = tipo;
        this.classe = classe;
    }

    public ArrayList<LogOccurrenceModule> PerformServerConnection() {
        Resolver resolver;
        try {
            resolver = new SimpleResolver(this.host);
        } catch (UnknownHostException ex) {
            addToArray("Host inválido: " + ex.getMessage(), LogLevel.SEVERE);
            return getLogArray();
        }

        Name name;
        try {
            name = Name.fromString(this.dominio, Name.root);
        } catch (TextParseException ex) {
            addToArray("Domínio inválido: " + ex.getMessage(), LogLevel.SEVERE);
            return getLogArray();
        }

        int tipoInt;
        int classeInt;
        try {
            tipoInt = Type.value(tipo.toUpperCase());
            classeInt = DClass.value(classe.toUpperCase());
        } catch (IllegalArgumentException ex) {
            addToArray("Tipo ou classe inválido: " + ex.getMessage(), LogLevel.SEVERE);
            return getLogArray();
        }

        Record record = Record.newRecord(name, tipoInt, classeInt);
        Message query = Message.newQuery(record);

        Message response;
        long startTime = System.nanoTime();

        try {
            response = resolver.send(query);
        } catch (IOException ex) {
            addToArray("Erro ao enviar consulta DNS: " + ex.getMessage(), LogLevel.SEVERE);
            return getLogArray();
        }

        long endTime = System.nanoTime();
        double elapsedMillis = (endTime - startTime) / 1_000_000.0;

        Record[] answers = response.getSectionArray(Section.ANSWER);
        if (answers.length == 0) {
            addToArray("Nenhuma resposta recebida.", LogLevel.WARNING);
        } else {
            for (Record ans : answers) {
                addToArray("Resposta: " + ans.rdataToString(), LogLevel.FINE);
            }
            addToArray("Tempo de resolução DNS: " + elapsedMillis + " ms.", LogLevel.DEBUG);
        }

        return getLogArray();
    }

    private void addToArray(String input, LogLevel level) {
        LogOccurrenceModule log = new LogOccurrenceModule(input, level, getHost());
        this.LogArray.add(log);
    }

    public ArrayList<LogOccurrenceModule> getLogArray() {
        return LogArray;
    }

    public String getHost() {
        return host;
    }

    public String getDominio() {
        return dominio;
    }
}
