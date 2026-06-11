package ecoquest.estrutura;

import ecoquest.model.ItemTroca;

import java.util.ArrayList;
import java.util.List;

public class ListaEncadeadaItens {
    private NoItem cabeca;

    public void adicionar(ItemTroca item) {
        NoItem novoNo = new NoItem(item);
        if (cabeca == null) {
            cabeca = novoNo;
            return;
        }

        NoItem atual = cabeca;
        while (atual.proximo != null) {
            atual = atual.proximo;
        }
        atual.proximo = novoNo;
    }

    public List<ItemTroca> paraLista() {
        List<ItemTroca> itens = new ArrayList<>();
        NoItem atual = cabeca;
        while (atual != null) {
            itens.add(atual.valor);
            atual = atual.proximo;
        }
        return itens;
    }

    private static class NoItem {
        private final ItemTroca valor;
        private NoItem proximo;

        private NoItem(ItemTroca valor) {
            this.valor = valor;
        }
    }
}
