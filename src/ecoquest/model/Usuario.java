package ecoquest.model;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private final String nome;
    private int pontos;
    private int sequencia;
    private final List<String> missoesConcluidas;

    public Usuario(String nome, int pontos, int sequencia) {
        this.nome = nome;
        this.pontos = pontos;
        this.sequencia = sequencia;
        this.missoesConcluidas = new ArrayList<>();
    }

    public void adicionarPontos(int valor) {
        pontos += valor;
    }

    public void aumentarSequencia() {
        sequencia++;
    }

    public boolean concluirMissao(String id) {
        if (missoesConcluidas.contains(id)) {
            return false;
        }
        missoesConcluidas.add(id);
        return true;
    }

    public Nivel nivel() {
        if (pontos >= 700) return new Nivel(5, "Guardiao do Planeta");
        if (pontos >= 450) return new Nivel(4, "Lider Circular");
        if (pontos >= 250) return new Nivel(3, "Agente Sustentavel");
        if (pontos >= 100) return new Nivel(2, "Eco Aprendiz");
        return new Nivel(1, "Broto Verde");
    }

    public String getNome() {
        return nome;
    }

    public int getPontos() {
        return pontos;
    }

    public int getSequencia() {
        return sequencia;
    }

    public List<String> getMissoesConcluidas() {
        return missoesConcluidas;
    }
}
