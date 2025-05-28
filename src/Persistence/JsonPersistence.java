/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Persistence;

import Enum.Role;
import Persistence.Logs.LogPersistence;
import Persistence.Worker1.Worker1Persistence;
import Utils.HostConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author Mauros
 */
public class JsonPersistence {

    public static void salvarJsonEmAppData(String nomeArquivo, String conteudoJson, String opc) {
        String os = HostConfig.obterSistemaOperacional();
        String pastaBase;

        switch (os) {
            case "Windows":
                pastaBase = System.getenv("APPDATA"); // C:\Users\USER\AppData\Roaming
                break;
            case "Mac":
                pastaBase = System.getProperty("user.home") + "/Library/Application Support";
                break;
            case "Linux":
                pastaBase = System.getProperty("user.home") + "/.config";
                break;
            default:
                System.err.println("Sistema operacional não suportado.");
                return;
        }

        File pastaLog = new File(pastaBase, "NTA" + opc);
        if (!pastaLog.exists()) {
            boolean criada = pastaLog.mkdirs();
            if (!criada) {
                System.err.println("Não foi possível criar a pasta NTA.");
                return;
            }
        }

        File arquivoJson = new File(pastaLog, nomeArquivo);
        try (FileWriter writer = new FileWriter(arquivoJson)) {
            writer.write(conteudoJson);
            System.out.println("JSON atualizado com sucesso em: " + arquivoJson.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T carregarJsonAppdata(String nomeArquivo, Class<T> clazz, String opc) {
        String conteudoJson = lerJsonDeAppData(nomeArquivo, opc);

        if (conteudoJson == null || conteudoJson.isEmpty()) {
            System.out.println("Arquivo de configuração não encontrado: " + nomeArquivo);
            return null;
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(conteudoJson, clazz);
        } catch (Exception e) {
            System.err.println("Erro ao desserializar JSON para " + clazz.getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    private static String lerJsonDeAppData(String nomeArquivo, String opc) {
        try {
            String appDataPath = System.getenv("APPDATA");
            Path path = Paths.get(appDataPath, "NTA" + opc, nomeArquivo);
            if (Files.exists(path)) {
                return new String(Files.readAllBytes(path));
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<LogPersistence> carregarJsonAppdataLog(String nomeArquivo) {
        String conteudoJson = lerJsonDeAppData(nomeArquivo, "/Log");

        if (conteudoJson == null || conteudoJson.isEmpty()) {
            System.out.println("Arquivo de configuração não encontrado: " + nomeArquivo);
            return null;
        }

        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<List<LogPersistence>>() {
            }.getType();
            return gson.fromJson(conteudoJson, listType);
        } catch (Exception e) {
            System.err.println("Erro ao desserializar JSON para LogPersistence: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static List<Worker1Persistence> carregarJsonAppdataMonitoramento(String nomeArquivo) {
        String conteudoJson = lerJsonDeAppData(nomeArquivo, "/Monitoramento");

        if (conteudoJson == null || conteudoJson.isEmpty()) {
            System.out.println("Arquivo de configuração não encontrado: " + nomeArquivo);
            return null;
        }

        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<List<Worker1Persistence>>() {
            }.getType();
            return gson.fromJson(conteudoJson, listType);
        } catch (Exception e) {
            System.err.println("Erro ao desserializar JSON para LogPersistence: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void adicionarEntradaAoLog(String nomeArquivo, String modulo, LogPersistence.SessionValues novaEntrada) {
        // Carrega todos os logs
        List<LogPersistence> todosLogs = carregarJsonAppdataLog(nomeArquivo);
        if (todosLogs == null) {
            todosLogs = new ArrayList<>();
        }

        // Procura pelo módulo existente
        LogPersistence moduloExistente = null;
        for (LogPersistence lp : todosLogs) {
            if (lp.module.equalsIgnoreCase(modulo)) {
                moduloExistente = lp;
                break;
            }
        }

        // Se não existir, cria um novo
        if (moduloExistente == null) {
            moduloExistente = new LogPersistence();
            moduloExistente.module = modulo;
            moduloExistente.session = new ArrayList<>();
            todosLogs.add(moduloExistente);
        }

        // Adiciona a nova entrada ao módulo correspondente
        moduloExistente.session.add(novaEntrada);

        // Salva no disco
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonAtualizado = gson.toJson(todosLogs);
        salvarJsonEmAppData(nomeArquivo, jsonAtualizado, "/Log");
    }
}
