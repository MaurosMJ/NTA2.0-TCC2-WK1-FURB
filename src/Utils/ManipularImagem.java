/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Mauros
 */
public class ManipularImagem {

    public static String copiarImagemParaAppData(String caminhoOrigem) {
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
                return null;
        }

        File pastaDestino = new File(pastaBase, "NTA/Imagens");
        if (!pastaDestino.exists()) {
            boolean criada = pastaDestino.mkdirs();
            if (!criada) {
                System.err.println("Não foi possível criar a pasta de destino.");
                return null;
            }
        }

        // Arquivo de origem e destino
        File arquivoOrigem = new File(caminhoOrigem);
        File arquivoDestino = new File(pastaDestino, arquivoOrigem.getName());

        // Copiando o arquivo
        try (FileInputStream fis = new FileInputStream(arquivoOrigem);
                FileOutputStream fos = new FileOutputStream(arquivoDestino)) {

            byte[] buffer = new byte[1024];
            int bytesLidos;
            while ((bytesLidos = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesLidos);
            }

            System.out.println("Imagem copiada com sucesso para: " + arquivoDestino.getAbsolutePath());
            return arquivoDestino.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("Erro ao copiar a imagem:");
            e.printStackTrace();
        }
        return null;
    }
}
