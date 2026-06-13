const modal = document.getElementById("authModal");
const tabEntrar = document.getElementById("tabEntrar");
const tabCadastrar = document.getElementById("tabCadastrar");
const formEntrar = document.getElementById("formEntrar");
const formCadastrar = document.getElementById("formCadastrar");
const erroEntrar = document.getElementById("erroEntrar");
const erroCadastrar = document.getElementById("erroCadastrar");

function abrirModal(aba) {
  trocarAba(aba || "entrar");
  modal.showModal();
}

function fecharModal() {
  modal.close();
  limparErros();
  formEntrar.reset();
  formCadastrar.reset();
}

function trocarAba(aba) {
  const isEntrar = aba === "entrar";
  tabEntrar.classList.toggle("ativa", isEntrar);
  tabCadastrar.classList.toggle("ativa", !isEntrar);
  formEntrar.classList.toggle("oculto", !isEntrar);
  formCadastrar.classList.toggle("oculto", isEntrar);
  limparErros();
}

function limparErros() {
  erroEntrar.textContent = "";
  erroCadastrar.textContent = "";
}

function setBotaoCarregando(botao, carregando) {
  botao.disabled = carregando;
  botao.dataset.textoOriginal = botao.dataset.textoOriginal || botao.textContent;
  botao.textContent = carregando ? "Aguarde..." : botao.dataset.textoOriginal;
}

async function fazerLogin(event) {
  event.preventDefault();
  const email = document.getElementById("loginEmail").value.trim();
  const senha = document.getElementById("loginSenha").value;
  const botao = formEntrar.querySelector("button[type=submit]");

  setBotaoCarregando(botao, true);
  try {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, senha })
    });
    const dados = await res.json();
    if (!res.ok) {
      erroEntrar.textContent = dados.erro || "Erro ao fazer login.";
      return;
    }
    localStorage.setItem("ecoquest_user", JSON.stringify({ email: dados.email, nome: dados.estado.usuario.nome }));
    window.location.href = "/app.html";
  } catch {
    erroEntrar.textContent = "Nao foi possivel conectar ao servidor.";
  } finally {
    setBotaoCarregando(botao, false);
  }
}

async function fazerCadastro(event) {
  event.preventDefault();
  const nome = document.getElementById("cadNome").value.trim();
  const email = document.getElementById("cadEmail").value.trim();
  const senha = document.getElementById("cadSenha").value;
  const confirmar = document.getElementById("cadConfirmar").value;
  const botao = formCadastrar.querySelector("button[type=submit]");

  if (senha !== confirmar) {
    erroCadastrar.textContent = "As senhas nao coincidem.";
    return;
  }
  if (senha.length < 6) {
    erroCadastrar.textContent = "A senha deve ter pelo menos 6 caracteres.";
    return;
  }

  setBotaoCarregando(botao, true);
  try {
    const res = await fetch("/api/auth/cadastro", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nome, email, senha })
    });
    const dados = await res.json();
    if (!res.ok) {
      erroCadastrar.textContent = dados.erro || "Erro ao cadastrar.";
      return;
    }
    localStorage.setItem("ecoquest_user", JSON.stringify({ email: dados.email, nome: dados.nome }));
    window.location.href = "/app.html";
  } catch {
    erroCadastrar.textContent = "Nao foi possivel conectar ao servidor.";
  } finally {
    setBotaoCarregando(botao, false);
  }
}

document.getElementById("btnEntrar").addEventListener("click", () => abrirModal("entrar"));
document.getElementById("btnCriarConta").addEventListener("click", () => abrirModal("cadastrar"));
document.getElementById("fecharModal").addEventListener("click", fecharModal);
tabEntrar.addEventListener("click", () => trocarAba("entrar"));
tabCadastrar.addEventListener("click", () => trocarAba("cadastrar"));
formEntrar.addEventListener("submit", fazerLogin);
formCadastrar.addEventListener("submit", fazerCadastro);

modal.addEventListener("click", (e) => {
  if (e.target === modal) fecharModal();
});
