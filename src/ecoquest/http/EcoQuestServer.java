package ecoquest.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ecoquest.model.ItemTroca;
import ecoquest.service.EcoQuestService;
import ecoquest.util.CsvUsuarios;
import ecoquest.util.Json;
import ecoquest.util.Senha;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EcoQuestServer {
    private final int port;
    private final EcoQuestService service;

    public EcoQuestServer(int port) {
        this.port = port;
        this.service = new EcoQuestService();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/estado", this::handleEstado);
        server.createContext("/api/missoes", this::handleMissoes);
        server.createContext("/api/itens", this::handleItens);
        server.createContext("/api/reset", this::handleReset);
        server.createContext("/api/auth", this::handleAuth);
        server.createContext("/", this::handleStaticFiles);
        server.setExecutor(null);
        server.start();

        System.out.println("EcoQuest rodando em http://localhost:" + port);
    }

    private void handleEstado(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            send(exchange, 405, "{\"erro\":\"Metodo nao permitido\"}", "application/json");
            return;
        }
        send(exchange, 200, service.estadoJson(), "application/json");
    }

    private void handleMissoes(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST") || !path.endsWith("/concluir")) {
            send(exchange, 405, "{\"erro\":\"Metodo nao permitido\"}", "application/json");
            return;
        }

        String[] partes = path.split("/");
        if (partes.length < 4) {
            send(exchange, 400, "{\"erro\":\"Missao invalida\"}", "application/json");
            return;
        }

        String idMissao = partes[3];
        boolean concluida = service.concluirMissao(idMissao);
        if (!concluida) {
            send(exchange, 404, "{\"erro\":\"Missao nao encontrada ou ja concluida\"}", "application/json");
            return;
        }

        send(exchange, 200, service.estadoJson(), "application/json");
    }

    private void handleItens(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            send(exchange, 405, "{\"erro\":\"Metodo nao permitido\"}", "application/json");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String nome = Json.getString(body, "nome");
        String categoria = Json.getString(body, "categoria");
        String estado = Json.getString(body, "estado");
        String descricao = Json.getString(body, "descricao");

        if (nome.isBlank()) {
            send(exchange, 400, "{\"erro\":\"Nome do item e obrigatorio\"}", "application/json");
            return;
        }

        service.cadastrarItem(new ItemTroca(nome, categoria, estado, descricao));
        send(exchange, 201, service.estadoJson(), "application/json");
    }

    private void handleAuth(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (method.equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            send(exchange, 204, "", "text/plain");
            return;
        }

        if (!method.equalsIgnoreCase("POST")) {
            send(exchange, 405, "{\"erro\":\"Metodo nao permitido\"}", "application/json");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        if (path.endsWith("/cadastro")) {
            handleCadastro(exchange, body);
        } else if (path.endsWith("/login")) {
            handleLogin(exchange, body);
        } else if (path.endsWith("/reload")) {
            handleReload(exchange, body);
        } else {
            send(exchange, 404, "{\"erro\":\"Rota nao encontrada\"}", "application/json");
        }
    }

    private void handleCadastro(HttpExchange exchange, String body) throws IOException {
        String nome = Json.getString(body, "nome");
        String email = Json.getString(body, "email");
        String senha = Json.getString(body, "senha");

        if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
            send(exchange, 400, "{\"erro\":\"Todos os campos sao obrigatorios\"}", "application/json");
            return;
        }
        if (!email.contains("@")) {
            send(exchange, 400, "{\"erro\":\"Email invalido\"}", "application/json");
            return;
        }
        if (CsvUsuarios.emailExiste(email)) {
            send(exchange, 409, "{\"erro\":\"Email ja cadastrado\"}", "application/json");
            return;
        }

        CsvUsuarios.salvar(email, nome, Senha.hash(senha));
        service.carregarUsuario(email, nome, 0, 0, List.of());

        StringBuilder json = new StringBuilder("{");
        Json.prop(json, "ok", true).append(",");
        Json.prop(json, "nome", nome).append(",");
        Json.prop(json, "email", email);
        json.append("}");
        send(exchange, 201, json.toString(), "application/json");
    }

    private void handleLogin(HttpExchange exchange, String body) throws IOException {
        String email = Json.getString(body, "email");
        String senha = Json.getString(body, "senha");

        if (email.isBlank() || senha.isBlank()) {
            send(exchange, 400, "{\"erro\":\"Email e senha sao obrigatorios\"}", "application/json");
            return;
        }

        Map<String, String> usuario = CsvUsuarios.buscarPorEmail(email);
        if (usuario == null || !usuario.get("senhaHash").equals(Senha.hash(senha))) {
            send(exchange, 401, "{\"erro\":\"Email ou senha incorretos\"}", "application/json");
            return;
        }

        String missoesBruto = usuario.get("missoesConcluidas");
        List<String> missoes = (missoesBruto == null || missoesBruto.isBlank())
                ? List.of()
                : Arrays.asList(missoesBruto.split("\\|"));

        service.carregarUsuario(
                email,
                usuario.get("nome"),
                Integer.parseInt(usuario.get("pontos")),
                Integer.parseInt(usuario.get("sequencia")),
                missoes
        );

        StringBuilder prefixo = new StringBuilder("{");
        Json.prop(prefixo, "email", email).append(",");
        prefixo.append("\"estado\":");
        String estadoJson = service.estadoJson();
        prefixo.append(estadoJson).append("}");
        send(exchange, 200, prefixo.toString(), "application/json");
    }

    private void handleReload(HttpExchange exchange, String body) throws IOException {
        String email = Json.getString(body, "email");
        if (email.isBlank()) {
            send(exchange, 400, "{\"erro\":\"Email obrigatorio\"}", "application/json");
            return;
        }

        Map<String, String> usuario = CsvUsuarios.buscarPorEmail(email);
        if (usuario == null) {
            send(exchange, 404, "{\"erro\":\"Usuario nao encontrado\"}", "application/json");
            return;
        }

        String missoesBruto = usuario.get("missoesConcluidas");
        List<String> missoes = (missoesBruto == null || missoesBruto.isBlank())
                ? List.of()
                : Arrays.asList(missoesBruto.split("\\|"));

        service.carregarUsuario(
                email,
                usuario.get("nome"),
                Integer.parseInt(usuario.get("pontos")),
                Integer.parseInt(usuario.get("sequencia")),
                missoes
        );

        send(exchange, 200, service.estadoJson(), "application/json");
    }

    private void handleReset(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            send(exchange, 405, "{\"erro\":\"Metodo nao permitido\"}", "application/json");
            return;
        }

        service.resetar();
        send(exchange, 200, service.estadoJson(), "application/json");
    }

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String rawPath = exchange.getRequestURI().getPath();
        String fileName = rawPath.equals("/") ? "index.html" : rawPath.substring(1);
        Path path = Path.of(fileName).normalize();

        if (path.isAbsolute() || path.startsWith("..") || !Files.exists(path)) {
            send(exchange, 404, "Arquivo nao encontrado", "text/plain");
            return;
        }

        String contentType = contentType(path);
        byte[] bytes = Files.readAllBytes(path);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String contentType(Path path) {
        String name = path.toString().toLowerCase();
        if (name.endsWith(".html")) return "text/html";
        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".js")) return "application/javascript";
        return "text/plain";
    }

    private void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
