package ecoquest.model;

public class Conquista {
    private final String id;
    private final String titulo;
    private final String descricao;
    private final String icone;
    private final boolean desbloqueada;

    public Conquista(String id, String titulo, String descricao, String icone, boolean desbloqueada) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.icone = icone;
        this.desbloqueada = desbloqueada;
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

    public String getIcone() {
        return icone;
    }

    public boolean isDesbloqueada() {
        return desbloqueada;
    }
}
