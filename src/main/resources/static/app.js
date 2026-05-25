


const API = 'http://localhost:8080';

//  UTILITIES (Funções reutilizáveis)

// Controla qual tela está visível (login, cadastro, dashboard)
function showScreen(id) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById(id).classList.add('active');
}

// Exibe mensagens rápidas para o usuário (feedback visual)
function toast(msg, isError = false) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className = 'toast show' + (isError ? ' error' : '');

  // Remove automaticamente após alguns segundos
  clearTimeout(t._timer);
  t._timer = setTimeout(() => t.classList.remove('show'), 3500);
}

// Formata número para moeda brasileira (R$)
function formatBRL(val) {
  return Number(val).toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  });
}

// Converte data do padrão ISO (yyyy-mm-dd) para brasileiro
function formatDate(d) {
  if (!d) return '–';

  const parts = d.split('-');
  return parts.length === 3
    ? `${parts[2]}/${parts[1]}/${parts[0]}`
    : d;
}

// Ativa/desativa botão com estado de carregamento
function setLoading(btnId, loading) {
  const btn = document.getElementById(btnId);
  if (!btn) return;

  btn.disabled = loading;
  btn.innerHTML = loading
    ? '<span class="loading"></span>'
    : btn.dataset.label || btn.textContent;
}

// Mostra mensagem de erro em tela (formulários)
function showError(id, msg) {
  const el = document.getElementById(id);
  el.textContent = msg;
  el.classList.add('show');
}

// Esconde erro
function hideError(id) {
  document.getElementById(id).classList.remove('show');
}

// Controle  modal
function openModal(id)  {
document.getElementById(id).classList.add('open');
 }
function closeModal(id) {
 document.getElementById(id).classList.remove('open');
 }


// Centraliza todas as requisições HTTP para o backend
async function apiFetch(url, opts = {}) {
  const res = await fetch(API + url, {
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // mantém sessão (login)
    ...opts
  });

  // Tratamento padrão de erro
  if (!res.ok) {
    let msg = 'Erro na requisição';
    try {
      const j = await res.json();
      msg = j.message || msg;
    } catch {}

    throw new Error(msg);
  }

  // Retorna JSON (se existir)
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}



//  AUTENTICAÇÃO


// Realiza login do usuário
async function doLogin() {
  hideError('loginError');

  const email = document.getElementById('loginEmail').value.trim();
  const senha = document.getElementById('loginSenha').value;

  // Validação básica
  if (!email || !senha) {
    showError('loginError', 'Preencha todos os campos.');
    return;
  }

  setLoading('loginBtn', true);

  try {
    // Requisição para o backend (Spring)
    const user = await apiFetch('/usuario/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha })
    });

    // Salva usuário na sessão do front
    currentUser = user;

    // Inicializa sistema
    initDashboard();

  } catch (e) {
    showError('loginError', e.message || 'E-mail ou senha inválidos.');
  } finally {
    setLoading('loginBtn', false);
  }
}

// Cadastro de novo usuário
async function doCadastro() {
  hideError('cadastroError');

  const nome  = document.getElementById('cadastroNome').value.trim();
  const email = document.getElementById('cadastroEmail').value.trim();
  const senha = document.getElementById('cadastroSenha').value;

  // Validações
  if (!nome || !email || !senha) {
    showError('cadastroError', 'Preencha todos os campos.');
    return;
  }

  if (senha.length < 6) {
    showError('cadastroError', 'Senha deve ter ao menos 6 caracteres.');
    return;
  }

  setLoading('cadastroBtn', true);

  try {
    await apiFetch('/usuario/cadastro', {
      method: 'POST',
      body: JSON.stringify({ nome, email, senha })
    });

    toast('Conta criada com sucesso!');
    showScreen('loginScreen');

  } catch (e) {
    showError('cadastroError', e.message || 'Erro ao criar conta.');
  } finally {
    setLoading('cadastroBtn', false);
  }
}
//pegar ususario por sessao
async function carregarUsuario() {
  try {
    currentUser = await apiFetch('/usuario/me');
    console.log("Usuário carregado:", currentUser);
  } catch (e) {
    toast('Usuário não autenticado', true);
  }
}
// Logout simples (limpa sessão do front)
function doLogout() {
  currentUser = null;
  showScreen('loginScreen');
}



//  DASHBOARD


// Inicializa a aplicação após login
function initDashboard() {
  document.getElementById('topbarUser').textContent =
    '👤 ' + (currentUser.nome || currentUser.email);

  showScreen('dashboard');
  switchTab('gastos');
  carregarGastos();
}


//
//  NAVEGAÇÃO INTERNA (TABS)
//

// Controla troca entre abas do sistema
function switchTab(tab) {
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));

  const tabs = ['gastos', 'metas', 'analise'];
  const idx  = tabs.indexOf(tab);

  document.querySelectorAll('.tab-btn')[idx].classList.add('active');
  document.getElementById('tab-' + tab).classList.add('active');

  // Carrega dados conforme aba
  if (tab === 'gastos')  carregarGastos();
  if (tab === 'metas')   carregarMetas();
  if (tab === 'analise') carregarAnalise();
}


//
//  PAINEL VISUAL DE MESES
//
const NOMES_MESES = ['Janeiro','Fevereiro','Março','Abril','Maio','Junho','Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'];
async function renderMeses() {
  const grid = document.getElementById('mesesGrid');
  grid.innerHTML = '';

  const ano      = new Date().getFullYear();
  const mesAtual = new Date().getMonth();

  document.getElementById('mesesAno').textContent = ano;

  let gastos = [], metas = [];
  try {
    gastos = await apiFetch(`/gastos?idUsuario=${currentUser.id}`) || [];
    metas  = await apiFetch(`/metas/usuario/${currentUser.id}`)    || [];
  } catch {}

  NOMES_MESES.forEach((nome, idx) => {
    const isFuturo = idx > mesAtual;
    const isAtual  = idx === mesAtual;

    const gastosDoMes = gastos.filter(g => {
      if (!g.dataGasto) return false;
      const d = new Date(g.dataGasto + 'T00:00:00');
      return d.getFullYear() === ano && d.getMonth() === idx;
    });
    const totalGastos = gastosDoMes.reduce((s, g) => s + parseFloat(g.valor || 0), 0);

    const metaDoMes = metas.find(m =>
      m.mes && m.mes.toLowerCase().includes(nome.toLowerCase())
    );

    let classe = '', valorLabel = '–';

    if (!isFuturo && metaDoMes && gastosDoMes.length > 0) {
      const limiteMeta = parseFloat(metaDoMes.valor || 0)
      const saldo = limiteMeta - totalGastos;
      if      (saldo > 0) {
      classe = 'lucro';
       valorLabel = '+ ' + formatBRL(saldo);
      }
      else {
      classe = 'prejuizo';
      valorLabel = '- ' + formatBRL(Math.abs(saldo));
       }
    } else if (!isFuturo && gastosDoMes.length > 0) {
      valorLabel = formatBRL(totalGastos);
    }

    const card = document.createElement('div');
    card.className = [
      'mes-card',
      classe,
      isAtual  ? 'atual'  : '',
      isFuturo ? 'futuro' : ''
    ].filter(Boolean).join(' ');

    card.innerHTML = `
      <div class="mes-nome">${nome}</div>
      <div class="mes-valor">${valorLabel}</div>
    `;

    if (!isFuturo && gastosDoMes.length > 0) {
      card.title = `${nome}: ${gastosDoMes.length} gasto(s) · Total ${formatBRL(totalGastos)}`;
    }

    grid.appendChild(card);
  });
}


//  GASTOS (CRUD)

// Cria novo gasto
async function salvarGasto() {
  const descricao = document.getElementById('gDescricao').value;
  const valor = parseFloat(document.getElementById('gValor').value);
  const dataGasto = document.getElementById('gData').value;
  const categoria = document.getElementById('gCategoria').value;
  const formaPagamento = document.getElementById('gFormaPagamento').value;
  const totalParcelas = parseInt(document.getElementById('gTotalParcelas').value);

  if (!valor || valor <= 0) {
    toast('Valor inválido', true);
    return;
  }

  try {
    await apiFetch('/gastos', {
      method: 'POST',
      body: JSON.stringify({
        descricao,
        valor,
        dataGasto,
        categoria,
        formaPagamento,
        totalParcelas,
        usuario: { id: currentUser.id }
      })
    });

    toast('Salvo!');
    carregarGastos();

  } catch (e) {
    console.log(e);
    toast('Erro ao salvar', true);
  }
}
//controlar parcelas de gastos
function controlarCamposParcela() {
    const formaPagamento = document.getElementById("gFormaPagamento").value;
    const grupoParcelas = document.getElementById("grupoParcelas");
    const inputTotalParcelas = document.getElementById("gTotalParcelas");

    if (formaPagamento === "credito") {
        grupoParcelas.style.display = "block"; // Mostra o campo
    } else {
        grupoParcelas.style.display = "none";  // Esconde o campo
        inputTotalParcelas.value = 1;          // Reseta para 1 se mudar de ideia
    }
}
//editar
function abrirEdicaoGasto(id, descricao, valor,dataGasto, categoria, formaPagamento, totalParcelas) {
  document.getElementById('editGastoId').value = id;
  document.getElementById('editGDescricao').value = descricao;
  document.getElementById('editGValor').value = valor;
  document.getElementById('editGData').value = dataGasto;
  document.getElementById('editGCategoria').value = categoria;
  document.getElementById('editGFormaPagamento').value = formaPagamento;
  document.getElementById('editGTotalParcelas').value = totalParcelas;


  openModal('editGastoModal');
}
async function confirmarEdicaoGasto() {
  const id = document.getElementById('editGastoId').value;
  const descricao = document.getElementById('editGDescricao').value;
  const valor = parseFloat(document.getElementById('editGValor').value);
  const dataGasto = document.getElementById("editGData").value;
  const categoria = document.getElementById('editGCategoria').value;
  const formaPagamento = document.getElementById('editGFormaPagamento').value;
  const totalParcelas = parseInt(document.getElementById('editGTotalParcelas').value) || 1;

  if (!valor || valor <= 0) {
    toast('Valor inválido', true);
    return;
  }

  if (!dataGasto) {
    toast('Data obrigatória', true);
    return;
  }

  try {
    await apiFetch(`/gastos/${id}`, {
      method: 'PUT',
      body: JSON.stringify({
        descricao,
        valor,
        dataGasto,
        categoria,
        formaPagamento,
        totalParcelas,
        usuario: { id: currentUser.id }
      })
    });

    closeModal('editGastoModal');
    toast('Gasto atualizado!');
    carregarGastos();

  } catch (e) {
    console.log(e);
    toast('Erro ao editar: ' + e.message, true);
  }
}
// card de gasto total

function atualizarStatsGeral(gastos) {
  const statTotal = document.getElementById('statTotal');
  if (!statTotal) return;

  const anoAtual = new Date().getFullYear();
  const mesAtual = new Date().getMonth();

  // Filtra para somar apenas o que pertence ao mês e ano vigentes
  const totalMesAtual = gastos.reduce((soma, g) => {
    if (!g.dataGasto) return soma;


    const d = new Date(g.dataGasto + 'T00:00:00');

    if (d.getFullYear() === anoAtual && d.getMonth() === mesAtual) {
      return soma + parseFloat(g.valor || 0);
    }
    return soma;
  }, 0);

  statTotal.textContent = formatBRL(totalMesAtual);
}
// Busca gastos do backend
async function carregarGastos(query = "") {
  try {

    const endpoint = query ? `/gastos${query}` : `/gastos?idUsuario=${currentUser.id}`;

    const gastos = await apiFetch(endpoint);

    renderGastos(gastos || []);


    if (typeof renderizarGridMeses === 'function') {

        renderizarGridMeses();
    } else if (typeof renderMeses === 'function') {
        renderMeses();
    }

  } catch (e) {
    console.error(e);
    toast('Erro ao carregar gastos', true);
  }
}
//filtragem
// Função para aplicar os filtros
async function aplicarFiltro() {
  const query = new URLSearchParams({
    idUsuario: currentUser.id,
    categoria: document.getElementById('filtroCategoria').value || "",
    dataInicio: document.getElementById('filtroDataInicio').value || "",
    dataFinal: document.getElementById('filtroDataFim').value || "",
    valMin: document.getElementById('filtroValMin').value || "",
    valMax: document.getElementById('filtroValMax').value || ""
  });

  // Isso vai gerar algo como: ?idUsuario=1&categoria=Saude&dataInicio=2023-01-01...
  carregarGastos(`?${query.toString()}`);
}

// Função para limpar os filtros
function limparFiltros() {
  document.getElementById('filtroCategoria').value = '';
  document.getElementById('filtroDataInicio').value = '';
  document.getElementById('filtroDataFim').value = '';
  document.getElementById('filtroValMin').value = '';
  document.getElementById('filtroValMax').value = '';

  carregarGastos(); // Recarrega sem filtros
}
// Mostra gastos na tabela
function renderGastos(gastos) {
  atualizarStatsGeral(gastos);

  const tbody = document.getElementById('gastosTabela');

  if (!gastos.length) {
    tbody.innerHTML = `<tr><td colspan="5">Nenhum gasto</td></tr>`;
    return;
  }

  tbody.innerHTML = gastos.map(g => `
    <tr>
      <td>${g.descricao}</td>
      <td>${g.categoria}</td>
      <td>${formatBRL(g.valor)}</td>
      <td>${formatBRL(g.valorParcela)}</td>
      <td>${formatDate(g.dataGasto)}</td>
      <td>${g.formaPagamento}</td>
      <td>${g.totalParcelas}</td>
      <td>${g.parcelaAtual}</td>
      <td>

      <button onclick="abrirEdicaoGasto(
                ${g.id},
                '${(g.descricao || '').replace(/'/g, "\\'")}',
                ${g.valor},
                '${g.dataGasto}',
                '${(g.categoria || '').replace(/'/g, "\\'")}'
              )">
                Editar
      </button>

        <button onclick="deletarGasto(${g.id})">Excluir</button>
      </td>
    </tr>
  `).join('');
}


// Remove gasto
async function deletarGasto(id) {
  if (!confirm('Excluir?')) return;

  try {
    await apiFetch(`/gastos/${id}`, { method: 'DELETE' });
    carregarGastos();
  } catch {
    toast('Erro ao excluir', true);
  }
}




//  METAS / ANÁLISE


// Salvar nova meta
async function salvarMeta() {
  const mes = document.getElementById('mMes').value;
  const renda = parseFloat(document.getElementById('mRenda').value);
  const valor = parseFloat(document.getElementById('mValor').value);

  if (!mes || !renda || !valor) {
    toast('Preencha todos os campos', true);
    return;
  }

  try {
    await apiFetch('/metas', {
      method: 'POST',
      body: JSON.stringify({
        mes,
        rendaMensal: renda,
        valor: valor,
        usuario: { id: currentUser.id }
      })
    });

    toast('Meta salva!');
    carregarMetas(); // recarrega lista

  } catch (e) {
    console.log(e);
    toast('Erro ao salvar meta', true);
  }
}

// editar metas
function abrirEdicaoMeta(id, mes, renda, valor) {
  document.getElementById('editMetaId').value = id;
  document.getElementById('editMMes').value = mes;
  document.getElementById('editMRenda').value = renda;
  document.getElementById('editMValor').value = valor;

  openModal('editMetaModal');
}

//---------------------//
async function confirmarEdicaoMeta() {
  const id = document.getElementById('editMetaId').value;
  const mes = document.getElementById('editMMes').value;
  const renda = parseFloat(document.getElementById('editMRenda').value);
  const valor = parseFloat(document.getElementById('editMValor').value);

  if (!mes || !renda || !valor) {
    toast('Preencha todos os campos', true);
    return;
  }

  try {
    await apiFetch(`/metas/${id}`, {
      method: 'PUT',
      body: JSON.stringify({
        mes,
        rendaMensal: renda,
        valor,
        usuario: { id: currentUser.id }
      })
    });

    closeModal('editMetaModal');
    toast('Meta atualizada!');
    carregarMetas();

  } catch (e) {
    console.log(e);
    toast('Erro ao editar meta', true);
  }
}

//mostrar metas na tabela:
function renderMetas(metas) {
  const tbody = document.getElementById('metasTabela');

  if (!metas.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="4">
          <div class="empty">
            <div class="icon">🎯</div>
            Nenhuma meta cadastrada.
          </div>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = metas.map(m => {
    const id = m.idMeta || m.id;

    return `
      <tr>
        <td>${m.mes}</td>
        <td>${formatBRL(m.rendaMensal)}</td>
        <td>${formatBRL(m.valor)}</td>
        <td>
          <button onclick="abrirEdicaoMeta(${id}, '${m.mes}', ${m.rendaMensal}, ${m.valor})">
            Editar
          </button>

          <button onclick="deletarMeta(${id})">
            Excluir
          </button>
        </td>
      </tr>
    `;
  }).join('');
}


//remove metas
async function deletarMeta(id) {
  if (!confirm('Excluir?')) return;

  try {
    await apiFetch(`/metas/${id}`, { method: 'DELETE' });
    carregarMetas();
  } catch {
    toast('Erro ao excluir', true);
  }
}

//---------------------------//
async function carregarMetas() {
  try {
    const metas = await apiFetch(`/metas/usuario/${currentUser.id}`);
    console.log("METAS:", metas); // debug
    renderMetas(metas || []);
  } catch {
    toast('Erro metas', true);
  }
}

//----------------------------//
async function carregarAnalise() {
  try {
    // Faz uma única chamada para o novo endpoint
    const res = await apiFetch(`/metas/analise-completa/${currentUser.id}`);

    // Referências do HTML
    const situacaoText = document.getElementById('situacaoText');
    const previsaoText = document.getElementById('previsaoText');
    const situacaoBox = document.getElementById('situacaoBox');
    const previsaoBox = document.getElementById('previsaoBox');
    const analyseEmpty = document.getElementById('analyseEmpty');

    // Preenche os textos com as chaves do Map retornado pelo Java
    situacaoText.innerText = res.situacao;
    previsaoText.innerText = res.previsao;

    // Controla a visibilidade
    situacaoBox.style.display = 'block';
    previsaoBox.style.display = 'block';
    analyseEmpty.style.display = 'none';

  } catch (e) {
    console.error("Erro na integração:", e);
    toast('Erro ao carregar análise financeira', true);
  }
}




// Enter = enviar formulário | ESC = fechar modal
document.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    if (document.getElementById('loginScreen').classList.contains('active')) doLogin();
    if (document.getElementById('cadastroScreen').classList.contains('active')) doCadastro();
  }

  if (e.key === 'Escape') {
    closeModal('editGastoModal');
  }
});

window.onload = () => {
  carregarUsuario();
};