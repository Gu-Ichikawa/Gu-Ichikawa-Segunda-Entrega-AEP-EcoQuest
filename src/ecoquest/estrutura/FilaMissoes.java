package ecoquest.estrutura;

import ecoquest.model.Missao;

import java.util.LinkedList;
import java.util.Queue;

public class FilaMissoes {
    private final Queue<Missao> fila;

    public FilaMissoes() {
        this.fila = new LinkedList<>();
    }

    public void enfileirar(Missao missao) {
        fila.add(missao);
    }

    public Missao proxima() {
        return fila.peek();
    }

    public void removerPorId(String id) {
        fila.removeIf(missao -> missao.getId().equals(id));
    }

    public int tamanho() {
        return fila.size();
    }
}
