package ecoquest.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvUsuarios {
    private static final String ARQUIVO = "usuarios.csv";
    private static final String CABECALHO = "email,nome,senhaHash,pontos,sequencia,missoesConcluidas";
    private static final int COL_EMAIL = 0;
    private static final int COL_NOME = 1;
    private static final int COL_SENHA = 2;
    private static final int COL_PONTOS = 3;
    private static final int COL_SEQ = 4;
    private static final int COL_MISSOES = 5;

    private CsvUsuarios() {}

    public static boolean emailExiste(String email) {
        return buscarPorEmail(email) != null;
    }

    public static void salvar(String email, String nome, String senhaHash) throws IOException {
        Path path = Path.of(ARQUIVO);
        boolean novo = !Files.exists(path);
        try (BufferedWriter writer = Files.newBufferedWriter(path,
                StandardCharsets.UTF_8,
                novo ? StandardOpenOption.CREATE : StandardOpenOption.APPEND,
                StandardOpenOption.WRITE)) {
            if (novo) {
                writer.write(CABECALHO);
                writer.newLine();
            }
            writer.write(email + "," + nome + "," + senhaHash + ",0,0,");
            writer.newLine();
        }
    }

    public static Map<String, String> buscarPorEmail(String email) {
        Path path = Path.of(ARQUIVO);
        if (!Files.exists(path)) return null;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean primeiraLinha = true;
            while ((line = reader.readLine()) != null) {
                if (primeiraLinha) { primeiraLinha = false; continue; }
                String[] partes = line.split(",", 6);
                if (partes.length >= 5 && partes[COL_EMAIL].equalsIgnoreCase(email)) {
                    Map<String, String> mapa = new LinkedHashMap<>();
                    mapa.put("email", partes[COL_EMAIL]);
                    mapa.put("nome", partes[COL_NOME]);
                    mapa.put("senhaHash", partes[COL_SENHA]);
                    mapa.put("pontos", partes[COL_PONTOS]);
                    mapa.put("sequencia", partes[COL_SEQ]);
                    mapa.put("missoesConcluidas", partes.length > COL_MISSOES ? partes[COL_MISSOES] : "");
                    return mapa;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public static void atualizarProgresso(String email, int pontos, int sequencia, List<String> missoesConcluidas) throws IOException {
        Path path = Path.of(ARQUIVO);
        if (!Files.exists(path)) return;
        List<String> linhas = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<String> novas = new ArrayList<>();
        for (String linha : linhas) {
            String[] partes = linha.split(",", 6);
            if (partes.length >= 3 && partes[COL_EMAIL].equalsIgnoreCase(email)) {
                String missoes = String.join("|", missoesConcluidas);
                novas.add(email + "," + partes[COL_NOME] + "," + partes[COL_SENHA]
                        + "," + pontos + "," + sequencia + "," + missoes);
            } else {
                novas.add(linha);
            }
        }
        Files.write(path, novas, StandardCharsets.UTF_8);
    }
}
