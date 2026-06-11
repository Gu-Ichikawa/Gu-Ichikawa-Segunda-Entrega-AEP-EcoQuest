package ecoquest.model;

public class Missao {
    private final String id;
    private final String titulo;
    private final String descricao;
    private final int pontos;
    private final String categoria;
    private final String frequencia;
    private final String icone;

    public Missao(String id, String titulo, String descricao, int pontos, String categoria, String frequencia, String icone) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.pontos = pontos;
        this.categoria = categoria;
        this.frequencia = frequencia;
        this.icone = icone;
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getPontos() {
        return pontos;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public String getIcone() {
        return icone;
    }
}
