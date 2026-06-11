class PlataformaEcoQuest {
  constructor() {
    this.apiBase = "";
    this.estado = null;
    this.toastTimer = null;
    this.iniciarEventos();
    this.carregarEstado();
  }

  iniciarEventos() {
    document.querySelectorAll(".nav-button").forEach((button) => {
      button.addEventListener("click", () => this.trocarTela(button.dataset.view));
    });

    document.getElementById("itemForm").addEventListener("submit", (event) => {
      event.preventDefault();
      this.cadastrarItem();
    });

    document.getElementById("resetApp").addEventListener("click", () => this.resetarDemo());
  }

  async carregarEstado() {
    try {
      const response = await fetch(`${this.apiBase}/api/estado`);
      this.estado = await response.json();
      this.renderizar();
    } catch (error) {
      this.mostrarAviso("Inicie o backend Java para carregar os dados: java -cp out ecoquest.Main");
    }
  }

  async concluirMissao(id) {
    const response = await fetch(`${this.apiBase}/api/missoes/${id}/concluir`, { method: "POST" });
    if (!response.ok) {
      this.mostrarAviso("Nao foi possivel concluir esta missao.");
      return;
    }

    this.estado = await response.json();
    this.renderizar();
    this.mostrarAviso("Missao concluida. Pontos e ranking atualizados pelo backend Java.");
  }

  async cadastrarItem() {
    const nome = document.getElementById("itemName").value.trim();
    const categoria = document.getElementById("itemCategory").value;
    const estado = document.getElementById("itemCondition").value;
    const descricao = document.getElementById("itemDescription").value.trim();
    if (!nome) return;

    const response = await fetch(`${this.apiBase}/api/itens`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nome, categoria, estado, descricao })
    });

    if (!response.ok) {
      this.mostrarAviso("Nao foi possivel cadastrar o item.");
      return;
    }

    this.estado = await response.json();
    document.getElementById("itemForm").reset();
    this.renderizar();
    this.mostrarAviso("Item cadastrado na lista encadeada do backend. Pontos atualizados.");
  }

  async resetarDemo() {
    const response = await fetch(`${this.apiBase}/api/reset`, { method: "POST" });
    this.estado = await response.json();
    this.renderizar();
    this.mostrarAviso("Demo reiniciada pelo backend Java.");
  }

  trocarTela(viewId) {
    document.querySelectorAll(".nav-button").forEach((button) => {
      button.classList.toggle("active", button.dataset.view === viewId);
    });
    document.querySelectorAll(".view").forEach((view) => {
      view.classList.toggle("active", view.id === viewId);
    });
  }

  renderizar() {
    if (!this.estado) return;
    this.renderizarMetricas();
    this.renderizarProximaMissao();
    this.renderizarGrafico();
    this.renderizarMissoes();
    this.renderizarRanking();
    this.renderizarConquistas();
    this.renderizarItens();
  }

  renderizarMetricas() {
    const { usuario, conquistas } = this.estado;
    const desbloqueadas = conquistas.filter((conquista) => conquista.desbloqueada).length;
    document.getElementById("totalPoints").textContent = usuario.pontos;
    document.getElementById("levelLabel").textContent = `Nivel ${usuario.nivel.numero} - ${usuario.nivel.nome}`;
    document.getElementById("streakDays").textContent = `${usuario.sequencia} dias`;
    document.getElementById("completedCount").textContent = usuario.concluidas.length;
    document.getElementById("achievementCount").textContent = `${desbloqueadas}/${conquistas.length}`;
    document.getElementById("queueSize").textContent = `${this.estado.tamanhoFila} na fila`;
  }

  renderizarProximaMissao() {
    const container = document.getElementById("nextMissionCard");
    const missao = this.estado.proximaMissao;
    if (!missao) {
      container.innerHTML = `<p class="empty-state">Todas as missoes iniciais foram concluidas. Cadastre novos itens ou reinicie a demo para apresentar novamente.</p>`;
      return;
    }

    container.innerHTML = `
      <div class="mission-card compact">
        <div class="mission-top">
          <span class="mission-icon">${missao.icone}</span>
          <span class="pill">${missao.frequencia}</span>
        </div>
        <h3>${missao.titulo}</h3>
        <p>${missao.descricao}</p>
        <div class="mission-meta">
          <span>+${missao.pontos} pontos</span>
          <span>${missao.categoria}</span>
        </div>
        <button class="primary-button" type="button" data-complete="${missao.id}">Concluir proxima</button>
      </div>
    `;
    container.querySelector("[data-complete]").addEventListener("click", () => this.concluirMissao(missao.id));
  }

  renderizarGrafico() {
    const chart = document.getElementById("impactChart");
    const impacto = this.estado.impacto;
    const maximo = Math.max(100, ...Object.values(impacto));
    const cores = {
      Carbono: "var(--green)",
      Agua: "var(--blue)",
      Energia: "var(--yellow)",
      Residuos: "var(--rose)",
      Circular: "var(--green-dark)"
    };

    chart.innerHTML = Object.entries(impacto).map(([categoria, pontos]) => {
      const largura = Math.min(100, Math.round((pontos / maximo) * 100));
      return `
        <div class="chart-row">
          <div class="chart-title"><span>${categoria}</span><span>${pontos} pts</span></div>
          <div class="bar-track"><div class="bar-fill" style="width: ${largura}%; background: ${cores[categoria]}"></div></div>
        </div>
      `;
    }).join("");
  }

  renderizarMissoes() {
    const lista = document.getElementById("missionList");
    const template = document.getElementById("missionTemplate");
    lista.innerHTML = "";

    this.estado.missoes.forEach((missao) => {
      const card = template.content.cloneNode(true);
      const artigo = card.querySelector(".mission-card");
      artigo.querySelector(".mission-icon").textContent = missao.icone;
      artigo.querySelector(".mission-frequency").textContent = missao.frequencia;
      artigo.querySelector("h3").textContent = missao.titulo;
      artigo.querySelector("p").textContent = missao.descricao;
      artigo.querySelector(".points").textContent = `+${missao.pontos} pontos`;
      artigo.querySelector(".category").textContent = missao.categoria;

      const input = artigo.querySelector("input");
      const preview = artigo.querySelector(".proof-preview");
      input.addEventListener("change", () => {
        const arquivo = input.files[0];
        if (!arquivo) return;
        preview.src = URL.createObjectURL(arquivo);
        preview.classList.add("visible");
      });

      const botao = artigo.querySelector(".complete-button");
      botao.textContent = missao.concluida ? "Missao concluida" : "Concluir missao";
      botao.disabled = missao.concluida;
      botao.addEventListener("click", () => this.concluirMissao(missao.id));
      lista.appendChild(card);
    });
  }

  renderizarRanking() {
    const lista = document.getElementById("rankingList");
    lista.innerHTML = this.estado.ranking.map((pessoa, index) => `
      <article class="ranking-row ${pessoa.usuarioAtual ? "current-user" : ""}">
        <div class="ranking-left">
          <span class="rank-position">${index + 1}</span>
          <div>
            <strong>${pessoa.nome}</strong>
            <p class="empty-state">${pessoa.usuarioAtual ? "Voce" : "Participante EcoQuest"}</p>
          </div>
        </div>
        <strong>${pessoa.pontos} pts</strong>
      </article>
    `).join("");
  }

  renderizarConquistas() {
    const lista = document.getElementById("achievementList");
    lista.innerHTML = this.estado.conquistas.map((conquista) => `
      <article class="achievement-card ${conquista.desbloqueada ? "" : "locked"}">
        <span class="badge-icon">${conquista.icone}</span>
        <h3>${conquista.titulo}</h3>
        <p>${conquista.descricao}</p>
        <span class="pill">${conquista.desbloqueada ? "Desbloqueada" : "Bloqueada"}</span>
      </article>
    `).join("");
  }

  renderizarItens() {
    const lista = document.getElementById("itemList");
    const itens = this.estado.itens;
    if (!itens.length) {
      lista.innerHTML = `<p class="empty-state">Nenhum item cadastrado ainda.</p>`;
      return;
    }

    lista.innerHTML = itens.map((item, index) => `
      <article class="item-card">
        <div class="panel-header">
          <h3>${item.nome}</h3>
          <span class="pill">No ${index + 1}</span>
        </div>
        <p>${item.descricao}</p>
        <div class="item-tags">
          <span class="pill">${item.categoria}</span>
          <span class="pill">${item.estado}</span>
          <span class="pill">+${item.pontos} pts</span>
        </div>
      </article>
    `).join("");
  }

  mostrarAviso(mensagem) {
    const toast = document.getElementById("toast");
    toast.textContent = mensagem;
    toast.classList.add("show");
    window.clearTimeout(this.toastTimer);
    this.toastTimer = window.setTimeout(() => toast.classList.remove("show"), 3800);
  }
}

document.addEventListener("DOMContentLoaded", () => {
  new PlataformaEcoQuest();
});
