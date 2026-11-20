const API = "/api";

const NIVELES = { "dominado": "Dominado", "basico": "Básico", "no tiene": "No tiene" };
const CICLO = ["basico", "dominado", "no tiene"];
const ICONOS = {
  "Data Analyst": "📊", "Data Scientist": "🧪", "Data Engineer": "🛠️", "BI Analyst": "📈",
  "Backend Developer": "⚙️", "Frontend Developer": "🎨", "DevOps Engineer": "🚀", "QA Analyst": "🔍",
};

const state = {
  puestos: [],
  habilidades: [],
  nivelesUI: {},
  nivelesGuardados: {},
  puestoDemandaId: null,
  puestoRelacionadosId: null,
  puestoAlineacionId: null,
  estudianteId: null,
};

try { state.estudianteId = localStorage.getItem("careergps_estudiante_id"); } catch (e) { }

const $ = (id) => document.getElementById(id);
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

function esc(texto) {
  const d = document.createElement("div");
  d.textContent = String(texto);
  return d.innerHTML;
}

async function fetchJson(url, options) {
  let res;
  try {
    res = await fetch(url, options);
  } catch (e) {
    throw new Error("No se pudo conectar con el servidor. Intenta de nuevo en unos segundos.");
  }
  const body = await res.json().catch(() => null);
  if (!res.ok) {
    const err = new Error(body && body.mensaje ? body.mensaje : "Ocurrió un error (" + res.status + ")");
    err.status = res.status;
    throw err;
  }
  return body;
}

async function conReintentos(url) {
  const banner = $("banner-servidor");
  const timer = setTimeout(() => banner.classList.remove("hidden"), 2500);
  try {
    for (let i = 0; i < 30; i++) {
      try {
        const datos = await fetchJson(url);
        if (Array.isArray(datos)) return datos;
      } catch (e) { }
      await sleep(3000);
    }
    throw new Error("El servidor no respondió. Recarga la página en un momento.");
  } finally {
    clearTimeout(timer);
    banner.classList.add("hidden");
  }
}

function toast(mensaje, tipo) {
  const el = $("toast");
  el.textContent = mensaje;
  el.className = "toast show " + (tipo || "");
  clearTimeout(toast._t);
  toast._t = setTimeout(() => { el.className = "toast"; }, 3200);
}

function animarNumero(el, hasta, formato, ms, onFrame) {
  const inicio = performance.now();
  function paso(ahora) {
    const t = Math.min((ahora - inicio) / ms, 1);
    const valor = Math.round(hasta * (1 - Math.pow(1 - t, 3)));
    el.textContent = formato(valor);
    if (onFrame) onFrame(valor);
    if (t < 1) requestAnimationFrame(paso);
  }
  requestAnimationFrame(paso);
}

function mostrarVista(nombre) {
  document.querySelectorAll(".view").forEach((v) => v.classList.remove("active"));
  $("view-" + nombre).classList.add("active");
  document.querySelectorAll(".tab-btn").forEach((b) => {
    b.classList.toggle("active", b.dataset.view === nombre);
  });
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function irAlApp(vista) {
  mostrarVista(vista || "demanda");
  if (vista === "perfil") cargarMiPerfil();
}

function mostrarError(id, mensaje) {
  const el = $(id);
  el.textContent = mensaje;
  el.classList.remove("hidden");
}

function renderGrid(containerId, selectedId, onSelect) {
  const el = $(containerId);
  el.innerHTML = "";
  state.puestos.forEach((p) => {
    const btn = document.createElement("button");
    btn.className = "puesto-card" + (p.id === selectedId ? " selected" : "");
    btn.innerHTML =
      '<span class="puesto-icono">' + (ICONOS[p.nombre] || "💼") + '</span>' +
      '<span class="puesto-nombre">' + esc(p.nombre) + '</span>' +
      '<span class="puesto-meta">' + p.totalOfertas + ' ofertas</span>' +
      '<span class="tag ' + (p.disponible ? "tag-ok" : "tag-warn") + '">' +
      (p.disponible ? "Datos suficientes" : "Pocos datos") + '</span>';
    btn.onclick = () => onSelect(p.id);
    el.appendChild(btn);
  });
}

function refrescarPuestos() {
  renderGrid("puestos-demanda", state.puestoDemandaId, (id) => {
    state.puestoDemandaId = id; refrescarPuestos(); verDemanda();
  });
  renderGrid("puestos-relacionados", state.puestoRelacionadosId, (id) => {
    state.puestoRelacionadosId = id; refrescarPuestos(); verRelacionados();
  });
  renderGrid("puestos-alineacion", state.puestoAlineacionId, (id) => {
    state.puestoAlineacionId = id; refrescarPuestos(); verAlineacion();
  });
}

function esqueleto() {
  const html = '<div class="puesto-card skeleton"></div>'.repeat(4);
  ["puestos-demanda", "puestos-relacionados", "puestos-alineacion"].forEach((id) => { $(id).innerHTML = html; });
}

function renderStats() {
  const disponibles = state.puestos.filter((p) => p.disponible).length;
  const ofertas = state.puestos.reduce((a, p) => a + p.totalOfertas, 0);
  $("stats").innerHTML =
    '<div><strong data-n="' + state.puestos.length + '">0</strong><span>perfiles</span></div>' +
    '<div><strong data-n="' + ofertas + '">0</strong><span>ofertas analizadas</span></div>' +
    '<div><strong data-n="' + state.habilidades.length + '">0</strong><span>habilidades</span></div>' +
    '<div><strong data-n="' + disponibles + '">0</strong><span>con datos suficientes</span></div>';
  $("stats").querySelectorAll("strong").forEach((el) => {
    animarNumero(el, Number(el.dataset.n), (v) => String(v), 900);
  });
}

async function verDemanda() {
  const errorEl = $("demanda-error");
  const resultadoEl = $("demanda-resultado");
  errorEl.classList.add("hidden");
  resultadoEl.classList.add("hidden");

  if (!state.puestoDemandaId) return mostrarError("demanda-error", "Primero elige un puesto.");
  try {
    const demanda = await fetchJson(API + "/puestos/" + state.puestoDemandaId + "/demanda");
    $("demanda-titulo").textContent = demanda.puestoNombre;
    animarNumero($("demanda-total"), demanda.totalOfertas, (v) => v + " ofertas analizadas", 700);
    const barrasEl = $("demanda-barras");
    barrasEl.innerHTML = "";
    const fills = [];
    demanda.habilidades
      .slice()
      .sort((a, b) => b.porcentaje - a.porcentaje)
      .forEach((h, i) => {
        const fila = document.createElement("div");
        fila.className = "barra-fila";
        fila.innerHTML =
          '<div class="barra-label"><span>' + esc(h.nombre) + '</span><strong>0%</strong></div>' +
          '<div class="barra-track"><div class="barra-fill ' + (i % 2 === 0 ? "" : "c-lime") + '" style="width:0%"></div></div>';
        barrasEl.appendChild(fila);
        fills.push({ fila, porcentaje: h.porcentaje });
      });
    resultadoEl.classList.remove("hidden");
    requestAnimationFrame(() => requestAnimationFrame(() => {
      fills.forEach((f) => {
        f.fila.querySelector(".barra-fill").style.width = f.porcentaje + "%";
        animarNumero(f.fila.querySelector("strong"), Math.round(f.porcentaje), (v) => v + "%", 700);
      });
    }));
  } catch (e) {
    mostrarError("demanda-error", e.message);
  }
}

async function verRelacionados(puestoId) {
  const id = puestoId || state.puestoRelacionadosId;
  const errorEl = $("relacionados-error");
  errorEl.classList.add("hidden");
  if (!id) return mostrarError("relacionados-error", "Primero elige un puesto.");
  state.puestoRelacionadosId = id;
  refrescarPuestos();
  try {
    const relacionados = await fetchJson(API + "/puestos/" + id + "/relacionados");
    const puesto = state.puestos.find((p) => p.id === id);
    $("relacionados-titulo").textContent = "Puestos relacionados con " + (puesto ? puesto.nombre : "");
    const listaEl = $("relacionados-lista");
    listaEl.innerHTML = relacionados.length === 0
      ? '<p class="muted">No hay puestos con habilidades en común todavía.</p>'
      : "";
    relacionados.forEach((r) => {
      const pct = Math.round(r.similitud * 100);
      const fila = document.createElement("button");
      fila.className = "related-row";
      fila.innerHTML =
        '<div class="related-top"><span>' + (ICONOS[r.nombre] || "💼") + " " + esc(r.nombre) + '</span><strong>' + pct + '% similar ›</strong></div>' +
        '<div class="barra-track"><div class="barra-fill" style="width:0%"></div></div>';
      fila.onclick = () => {
        state.puestoDemandaId = r.puestoId;
        refrescarPuestos();
        mostrarVista("demanda");
        verDemanda();
      };
      listaEl.appendChild(fila);
      requestAnimationFrame(() => requestAnimationFrame(() => {
        fila.querySelector(".barra-fill").style.width = pct + "%";
      }));
    });
    $("relacionados-resultado").classList.remove("hidden");
    mostrarVista("relacionados");
  } catch (e) {
    $("relacionados-resultado").classList.add("hidden");
    mostrarError("relacionados-error", e.message);
  }
}

function nivelClase(nivel) {
  if (nivel === "dominado") return "nivel-dominado";
  if (nivel === "basico") return "nivel-basico";
  return "nivel-no-tiene";
}

function cambiosPendientes() {
  return Object.keys(state.nivelesUI).filter((id) => state.nivelesUI[id] !== state.nivelesGuardados[id]);
}

function renderChips() {
  const el = $("chips-habilidades");
  el.innerHTML = "";
  state.habilidades.forEach((h) => {
    const nivel = state.nivelesUI[h.id];
    const btn = document.createElement("button");
    btn.className = "skill-chip" + (nivel ? " " + nivelClase(nivel) : "");
    btn.innerHTML = "<span>" + esc(h.nombre) + "</span>" + (nivel ? "<small>" + NIVELES[nivel] + "</small>" : "");
    btn.onclick = () => {
      const actual = state.nivelesUI[h.id];
      if (!actual) {
        state.nivelesUI[h.id] = CICLO[0];
      } else {
        const siguiente = CICLO[CICLO.indexOf(actual) + 1];
        if (siguiente) state.nivelesUI[h.id] = siguiente;
        else if (state.nivelesGuardados[h.id]) state.nivelesUI[h.id] = CICLO[0];
        else delete state.nivelesUI[h.id];
      }
      renderChips();
    };
    el.appendChild(btn);
  });
  const n = cambiosPendientes().length;
  $("cambios-pendientes").textContent = n ? n + (n === 1 ? " cambio sin guardar" : " cambios sin guardar") : "";
}

function olvidarEstudiante() {
  state.estudianteId = null;
  state.nivelesGuardados = {};
  state.nivelesUI = {};
  try { localStorage.removeItem("careergps_estudiante_id"); } catch (e) { }
}

async function cargarMiPerfil() {
  if (!state.estudianteId) { renderChips(); return; }
  try {
    const est = await fetchJson(API + "/estudiantes/" + state.estudianteId);
    if (est && est.carrera) $("input-carrera").value = est.carrera;
    const lista = await fetchJson(API + "/estudiantes/" + state.estudianteId + "/habilidades");
    state.nivelesGuardados = {};
    lista.forEach((h) => { state.nivelesGuardados[h.habilidadId] = h.nivel; });
    state.nivelesUI = Object.assign({}, state.nivelesGuardados);
  } catch (e) {
    if (e.status === 404) olvidarEstudiante();
  }
  renderChips();
}

async function guardarPerfil() {
  const btn = $("btn-guardar-perfil");
  const statusEl = $("perfil-status");
  const carrera = $("input-carrera").value.trim();
  statusEl.className = "status-msg";
  statusEl.textContent = "";
  if (!carrera) {
    statusEl.className = "error-msg";
    statusEl.textContent = "Escribe tu carrera para guardar el perfil.";
    return;
  }
  btn.disabled = true;
  btn.textContent = "Guardando…";
  try {
    const opts = (method) => ({
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ carrera }),
    });
    if (state.estudianteId) {
      try {
        await fetchJson(API + "/estudiantes/" + state.estudianteId, opts("PUT"));
      } catch (e) {
        if (e.status !== 404) throw e;
        const previos = Object.assign({}, state.nivelesUI);
        olvidarEstudiante();
        state.nivelesUI = previos;
      }
    }
    if (!state.estudianteId) {
      const estudiante = await fetchJson(API + "/estudiantes", opts("POST"));
      state.estudianteId = estudiante.id;
      try { localStorage.setItem("careergps_estudiante_id", estudiante.id); } catch (e) { }
    }
    for (const id of cambiosPendientes()) {
      await fetchJson(API + "/estudiantes/" + state.estudianteId + "/habilidades", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ habilidadId: Number(id), nivel: state.nivelesUI[id] }),
      });
    }
    state.nivelesGuardados = Object.assign({}, state.nivelesUI);
    renderChips();
    toast("Perfil guardado ✓", "ok");
  } catch (e) {
    statusEl.className = "error-msg";
    statusEl.textContent = e.message;
  } finally {
    btn.disabled = false;
    btn.textContent = "Guardar perfil";
  }
}

async function verAlineacion() {
  const errorEl = $("alineacion-error");
  const resultadoEl = $("alineacion-resultado");
  errorEl.classList.add("hidden");
  resultadoEl.classList.add("hidden");

  if (!state.estudianteId) {
    return mostrarError("alineacion-error", "Primero registra tu perfil en la pestaña 'Mi perfil'.");
  }
  if (!state.puestoAlineacionId) return mostrarError("alineacion-error", "Elige un puesto para comparar.");
  try {
    const a = await fetchJson(API + "/estudiantes/" + state.estudianteId + "/alineacion/" + state.puestoAlineacionId);
    $("alineacion-titulo").textContent = "Tu alineación con " + a.puestoNombre;
    const chips = (lista, vacio) => lista.length
      ? lista.map((c) => "<span class='chip'>" + esc(c) + "</span>").join("")
      : "<span class='muted'>" + vacio + "</span>";
    $("alineacion-coincidencias").innerHTML = chips(a.coincidencias, "Ninguna todavía");
    $("alineacion-fortalecer").innerHTML = chips(a.aFortalecer, "¡Dominas todo lo que pide este puesto!");

    const total = a.coincidencias.length + a.aFortalecer.length;
    const pct = total ? Math.round(a.coincidencias.length * 100 / total) : 0;
    $("ring-texto").textContent = total
      ? "Cubres " + a.coincidencias.length + " de las " + total + " habilidades que pide este puesto."
      : "Este puesto todavía no tiene habilidades registradas.";
    const ring = $("ring");
    ring.style.setProperty("--p", 0);
    resultadoEl.classList.remove("hidden");
    animarNumero($("ring-num"), pct, (v) => v + "%", 900, (v) => ring.style.setProperty("--p", v));
  } catch (e) {
    if (e.status === 404 && /estudiante/i.test(e.message)) olvidarEstudiante();
    mostrarError("alineacion-error", e.message);
  }
}

document.querySelectorAll(".tab-btn").forEach((btn) => {
  btn.addEventListener("click", () => irAlApp(btn.dataset.view));
});
document.querySelectorAll("[data-go]").forEach((card) => {
  const ir = () => irAlApp(card.dataset.go);
  card.addEventListener("click", ir);
  card.addEventListener("keydown", (e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); ir(); } });
});
$("logo").addEventListener("click", () => mostrarVista("landing"));
$("btn-ingresar").addEventListener("click", () => irAlApp("demanda"));
$("btn-registrar-cta").addEventListener("click", () => irAlApp("perfil"));
$("btn-ver-relacionados-desde-demanda").addEventListener("click", () => verRelacionados(state.puestoDemandaId));
$("btn-guardar-perfil").addEventListener("click", guardarPerfil);

(async function init() {
  mostrarVista("landing");
  esqueleto();
  try {
    state.puestos = await conReintentos(API + "/puestos/resumen");
    state.habilidades = await conReintentos(API + "/habilidades");
    refrescarPuestos();
    renderStats();
    await cargarMiPerfil();
  } catch (e) {
    console.error("Error inicializando CareerGPS:", e);
    toast(e.message, "error");
  }
})();
