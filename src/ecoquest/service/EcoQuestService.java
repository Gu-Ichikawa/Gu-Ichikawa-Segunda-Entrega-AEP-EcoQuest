package ecoquest.service;

import ecoquest.estrutura.FilaMissoes;
import ecoquest.estrutura.ListaEncadeadaItens;
import ecoquest.model.Conquista;
import ecoquest.model.ItemTroca;
import ecoquest.model.Missao;
import ecoquest.model.Nivel;
import ecoquest.model.Usuario;
import ecoquest.util.CsvUsuarios;
import ecoquest.util.Json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EcoQuestService {
    private final List<Missao> missoes = new ArrayList<>();
    private final List<Map<String, Object>> rankingBase = new ArrayList<>();
    private Usuario usuario;
    private FilaMissoes filaMissoes;
    private ListaEncadeadaItens itens;
    private String emailAtual = null;

    public EcoQuestService() {
        criarMissoes();
        criarRankingBase();
        resetar();
    }

    public void resetar() {
        emailAtual = null;
        usuario = new Usuario("Joao Eco", 80, 3);
        itens = new ListaEncadeadaItens();
        itens.adicionar(new ItemTroca("Livro de algoritmos", "Livros", "Muito bom", "Livro usado para estudo, disponivel para troca por outro material academico."));
        itens.adicionar(new ItemTroca("Jaqueta jeans", "Roupas", "Bom", "Peca conservada, ideal para reuso em vez de descarte."));
        montarFila();
    }

    public void carregarUsuario(String email, String nome, int pontos, int sequencia, List<String> missoesConcluidas) {
        emailAtual = email;
        usuario = new Usuario(nome, pontos, sequencia);
        for (String id : missoesConcluidas) {
            usuario.concluirMissao(id);
        }
        itens = new ListaEncadeadaItens();
        montarFila();
    }

    public boolean concluirMissao(String id) {
        Missao missao = buscarMissao(id);
        if (missao == null || !usuario.concluirMissao(id)) {
            return false;
        }

        usuario.adicionarPontos(missao.getPontos());
        usuario.aumentarSequencia();
        filaMissoes.removerPorId(id);
        persistir();
        return true;
    }

    public void cadastrarItem(ItemTroca item) {
        itens.adicionar(item);
        usuario.adicionarPontos(item.getPontos());
        if (usuario.concluirMissao("troca")) {
            Missao missaoTroca = buscarMissao("troca");
            usuario.adicionarPontos(missaoTroca.getPontos());
            usuario.aumentarSequencia();
            filaMissoes.removerPorId("troca");
        }
        persistir();
    }

    private void persistir() {
        if (emailAtual == null) return;
        try {
            CsvUsuarios.atualizarProgresso(emailAtual, usuario.getPontos(), usuario.getSequencia(), usuario.getMissoesConcluidas());
        } catch (IOException e) {
            System.err.println("Erro ao persistir usuario: " + e.getMessage());
        }
    }

    public String estadoJson() {
        StringBuilder json = new StringBuilder();
        Nivel nivel = usuario.nivel();

        json.append("{");
        json.append("\"usuario\":{");
        Json.prop(json, "nome", usuario.getNome()).append(",");
        Json.prop(json, "pontos", usuario.getPontos()).append(",");
        Json.prop(json, "sequencia", usuario.getSequencia()).append(",");
        json.append("\"nivel\":{");
        Json.prop(json, "numero", nivel.numero()).append(",");
        Json.prop(json, "nome", nivel.nome());
        json.append("},");
        json.append("\"concluidas\":").append(Json.arrayStrings(usuario.getMissoesConcluidas()));
        json.append("},");

        json.append("\"missoes\":").append(missoesJson()).append(",");
        json.append("\"proximaMissao\":").append(missaoJson(filaMissoes.proxima())).append(",");
        Json.prop(json, "tamanhoFila", filaMissoes.tamanho()).append(",");
        json.append("\"itens\":").append(itensJson()).append(",");
        json.append("\"ranking\":").append(rankingJson()).append(",");
        json.append("\"conquistas\":").append(conquistasJson()).append(",");
        json.append("\"impacto\":").append(impactoJson());
        json.append("}");
        return json.toString();
    }

    private void criarMissoes() {
        missoes.add(new Missao("bike", "Ir de bicicleta ou transporte publico", "Registre um deslocamento com menor emissao de carbono.", 45, "Carbono", "Diaria", "BI"));
        missoes.add(new Missao("banho", "Tomar banho curto", "Economize agua mantendo o banho em ate 5 minutos.", 30, "Agua", "Diaria", "AG"));
        missoes.add(new Missao("energia", "Reduzir consumo de energia", "Desligue luzes e aparelhos fora de uso durante o dia.", 35, "Energia", "Diaria", "EN"));
        missoes.add(new Missao("reciclagem", "Separar residuos reciclaveis", "Separe papel, plastico, vidro ou metal para coleta correta.", 40, "Residuos", "Semanal", "RC"));
        missoes.add(new Missao("garrafa", "Usar garrafa reutilizavel", "Evite descartaveis levando sua propria garrafa.", 25, "Residuos", "Diaria", "GR"));
        missoes.add(new Missao("troca", "Cadastrar item para troca ou doacao", "Aumente a vida util de um objeto parado em casa.", 50, "Circular", "Semanal", "TR"));
    }

    private void criarRankingBase() {
        rankingBase.add(Map.of("nome", "Marina", "pontos", 610));
        rankingBase.add(Map.of("nome", "Rafael", "pontos", 520));
        rankingBase.add(Map.of("nome", "Bianca", "pontos", 340));
        rankingBase.add(Map.of("nome", "Turma ADS", "pontos", 210));
    }

    private void montarFila() {
        filaMissoes = new FilaMissoes();
        for (Missao missao : missoes) {
            if (!usuario.getMissoesConcluidas().contains(missao.getId())) {
                filaMissoes.enfileirar(missao);
            }
        }
    }

    private Missao buscarMissao(String id) {
        return missoes.stream()
                .filter(missao -> missao.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private String missoesJson() {
        List<String> blocos = new ArrayList<>();
        for (Missao missao : missoes) {
            blocos.add(missaoJson(missao));
        }
        return "[" + String.join(",", blocos) + "]";
    }

    private String missaoJson(Missao missao) {
        if (missao == null) return "null";

        StringBuilder json = new StringBuilder("{");
        Json.prop(json, "id", missao.getId()).append(",");
        Json.prop(json, "titulo", missao.getTitulo()).append(",");
        Json.prop(json, "descricao", missao.getDescricao()).append(",");
        Json.prop(json, "pontos", missao.getPontos()).append(",");
        Json.prop(json, "categoria", missao.getCategoria()).append(",");
        Json.prop(json, "frequencia", missao.getFrequencia()).append(",");
        Json.prop(json, "icone", missao.getIcone()).append(",");
        Json.prop(json, "concluida", usuario.getMissoesConcluidas().contains(missao.getId()));
        json.append("}");
        return json.toString();
    }

    private String itensJson() {
        List<String> blocos = new ArrayList<>();
        for (ItemTroca item : itens.paraLista()) {
            StringBuilder json = new StringBuilder("{");
            Json.prop(json, "nome", item.getNome()).append(",");
            Json.prop(json, "categoria", item.getCategoria()).append(",");
            Json.prop(json, "estado", item.getEstado()).append(",");
            Json.prop(json, "descricao", item.getDescricao()).append(",");
            Json.prop(json, "criadoEm", item.getCriadoEm()).append(",");
            Json.prop(json, "pontos", item.getPontos());
            json.append("}");
            blocos.add(json.toString());
        }
        return "[" + String.join(",", blocos) + "]";
    }

    private String rankingJson() {
        List<Map<String, Object>> ranking = rankingAtual();
        List<String> blocos = new ArrayList<>();
        for (Map<String, Object> pessoa : ranking) {
            StringBuilder json = new StringBuilder("{");
            Json.prop(json, "nome", pessoa.get("nome").toString()).append(",");
            Json.prop(json, "pontos", (int) pessoa.get("pontos")).append(",");
            Json.prop(json, "usuarioAtual", pessoa.get("nome").equals(usuario.getNome()));
            json.append("}");
            blocos.add(json.toString());
        }
        return "[" + String.join(",", blocos) + "]";
    }

    private String conquistasJson() {
        List<Conquista> conquistas = conquistas();
        List<String> blocos = new ArrayList<>();
        for (Conquista conquista : conquistas) {
            StringBuilder json = new StringBuilder("{");
            Json.prop(json, "id", conquista.getId()).append(",");
            Json.prop(json, "titulo", conquista.getTitulo()).append(",");
            Json.prop(json, "descricao", conquista.getDescricao()).append(",");
            Json.prop(json, "icone", conquista.getIcone()).append(",");
            Json.prop(json, "desbloqueada", conquista.isDesbloqueada());
            json.append("}");
            blocos.add(json.toString());
        }
        return "[" + String.join(",", blocos) + "]";
    }

    private List<Conquista> conquistas() {
        return List.of(
                new Conquista("primeira", "Primeira atitude", "Conclua sua primeira missao sustentavel.", "C1", usuario.getMissoesConcluidas().size() >= 1),
                new Conquista("cem", "100 pontos verdes", "Alcance 100 pontos no EcoQuest.", "C2", usuario.getPontos() >= 100),
                new Conquista("multicategoria", "Impacto diverso", "Conclua missoes em pelo menos 3 categorias.", "C3", categoriasConcluidas().size() >= 3),
                new Conquista("circular", "Agente circular", "Cadastre pelo menos 1 item para troca ou doacao.", "C4", !itens.paraLista().isEmpty()),
                new Conquista("ranking", "Top 3 sustentavel", "Entre nas tres primeiras posicoes do ranking.", "C5", usuarioEstaNoTop3())
        );
    }

    private boolean usuarioEstaNoTop3() {
        List<Map<String, Object>> ranking = rankingAtual();
        int limite = Math.min(3, ranking.size());
        for (int i = 0; i < limite; i++) {
            if (ranking.get(i).get("nome").equals(usuario.getNome())) {
                return true;
            }
        }
        return false;
    }

    private List<Map<String, Object>> rankingAtual() {
        List<Map<String, Object>> ranking = new ArrayList<>(rankingBase);
        ranking.add(Map.of("nome", usuario.getNome(), "pontos", usuario.getPontos()));
        ranking.sort(Comparator.comparingInt((Map<String, Object> pessoa) -> (int) pessoa.get("pontos")).reversed());
        return ranking;
    }

    private List<String> categoriasConcluidas() {
        List<String> categorias = new ArrayList<>();
        for (Missao missao : missoes) {
            if (usuario.getMissoesConcluidas().contains(missao.getId()) && !categorias.contains(missao.getCategoria())) {
                categorias.add(missao.getCategoria());
            }
        }
        return categorias;
    }

    private String impactoJson() {
        Map<String, Integer> impacto = new LinkedHashMap<>();
        impacto.put("Carbono", 0);
        impacto.put("Agua", 0);
        impacto.put("Energia", 0);
        impacto.put("Residuos", 0);
        impacto.put("Circular", 0);

        for (Missao missao : missoes) {
            if (usuario.getMissoesConcluidas().contains(missao.getId())) {
                impacto.put(missao.getCategoria(), impacto.get(missao.getCategoria()) + missao.getPontos());
            }
        }

        List<String> pares = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : impacto.entrySet()) {
            StringBuilder json = new StringBuilder();
            Json.prop(json, entry.getKey(), entry.getValue());
            pares.add(json.toString());
        }
        return "{" + String.join(",", pares) + "}";
    }
}
