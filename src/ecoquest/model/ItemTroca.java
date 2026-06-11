package ecoquest.model;

import java.time.LocalDateTime;

public class ItemTroca {
    private final String nome;
    private final String categoria;
    private final String estado;
    private final String descricao;
    private final String criadoEm;
    private final int pontos;

    public ItemTroca(String nome, String categoria, String estado, String descricao) {
        this.nome = nome;
        this.categoria = categoria == null || categoria.isBlank() ? "Outros" : categoria;
        this.estado = estado == null || estado.isBlank() ? "Bom" : estado;
        this.descricao = descricao == null || descricao.isBlank()
                ? "Item cadastrado para incentivar reuso e economia circular."
                : descricao;
        this.criadoEm = LocalDateTime.now().toString();
        this.pontos = 25;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getEstado() {
        return estado;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCriadoEm() {
        return criadoEm;
    }

    public int getPontos() {
        return pontos;
    }
}
