const API = "/api";

const NIVELES = { "dominado": "Dominado", "basico": "Básico", "no tiene": "No tiene" };

const state = {
  puestos: [],
  habilidades: [],
  habilidadesPendientes: [],
  puestoDemandaId: null,
  puestoRelacionadosId: null,
  puestoAlineacionId: null,
  estudianteId: null,
};

try { state.estudianteId = localStorage.getItem("careergps_estudiante_id"); } catch (e) { }

const $ = (id) => document.getElementById(id);

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
    const mensaje = body && body.mensaje ? body.mensaje : "Ocurrió un error (" + res.status + ")";
    const err = new Error(mensaje);
    err.status = res.status;
    throw err;
  }
  return body;
}

function toast(mensaje, tipo) {
  const el = $("toast");
  el.textContent = mensaje;
  el.className = "toast show " + (tipo || "");
  clearTimeout(toast._t);
  toast._t = setTimeout(() => { el.className = "toast"; }, 3200);
}

async function conCarga(btn, fn) {
  if (btn.disabled) return;
  const textoOriginal = btn.textContent;
  btn.disabled = true;
  btn.textContent = "Cargando…";
  try { await fn(); } finally { btn.disabled = false; btn.textContent = textoOriginal; }
}

function mostrarVista(nombre) {
  document.querySelectorAll(".view").forEach((v) => v.classList.remove("active"));
  $("view-" + nombre).classList.add("active");
  document.querySelectorAll(".tab-btn").forEach((b) => {
    b.classList.toggle("active", b.dataset.view === nombre);
  });
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function renderPillGroup(containerId, selectedId, onSelect) {
  const el = $(containerId);
  el.innerHTML = "";
  state.puestos.forEach((p) => {
    const btn = document.createElement("button");
    btn.className = "pill-select" + (p.id === selectedId ? " selected" : "");
    btn.textContent = p.nombre;
    btn.onclick = () => onSelect(p.id);
    el.appendChild(btn);
  });
}

function refrescarPills() {
  renderPillGroup("puestos-demanda", state.puestoDemandaId, (id) => {
    state.puestoDemandaId = id; refrescarPills(); verDemanda();
  });
  renderPillGroup("puestos-relacionados", state.puestoRelacionadosId, (id) => {
    state.puestoRelacionadosId = id; refrescarPills(); verRelacionados();
  });
  renderPillGroup("puestos-alineacion", state.puestoAlineacionId, (id) => {
    state.puestoAlineacionId = id; refrescarPills(); verAlineacion();
  });
}

async function cargarPuestos() {
  state.puestos = await fetchJson(API + "/puestos");
  refrescarPills();
}

function mostrarError(id, mensaje) {
  const el = $(id);
  el.textContent = mensaje;
  el.classList.remove("hidden");
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
    $("demanda-total").textContent = demanda.totalOfertas + " ofertas analizadas";
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
          '<div class="barra-label"><span>' + esc(h.nombre) + '</span><strong>' + Math.round(h.porcentaje) + '%</strong></div>' +
          '<div class="barra-track"><div class="barra-fill ' + (i % 2 === 0 ? "" : "c-lime") + '" style="width:0%"></div></div>';
        barrasEl.appendChild(fila);
        fills.push({ el: fila.querySelector(".barra-fill"), porcentaje: h.porcentaje });
      });
    resultadoEl.classList.remove("hidden");
    requestAnimationFrame(() => requestAnimationFrame(() => {
      fills.forEach((f) => { f.el.style.width = f.porcentaje + "%"; });
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
  refrescarPills();
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
      const fila = document.createElement("div");
      fila.className = "related-row";
      fila.innerHTML =
        '<div class="related-top"><span>' + esc(r.nombre) + '</span><strong>' + pct + '% similar</strong></div>' +
        '<div class="barra-track"><div class="barra-fill" style="width:' + pct + '%"></div></div>';
      listaEl.appendChild(fila);
    });
    $("relacionados-resultado").classList.remove("hidden");
    mostrarVista("relacionados");
  } catch (e) {
    $("relacionados-resultado").classList.add("hidden");
    mostrarError("relacionados-error", e.message);
  }
}

async function cargarHabilidades() {
  state.habilidades = await fetchJson(API + "/habilidades");
  const select = $("select-habilidad");
  select.innerHTML = "";
  state.habilidades.forEach((h) => {
    const opt = document.createElement("option");
    opt.value = h.id;
    opt.textContent = h.nombre;
    select.appendChild(opt);
  });
}

function agregarHabilidadPendiente() {
  const habilidadId = Number($("select-habilidad").value);
  const nivel = $("select-nivel").value;
  const habilidad = state.habilidades.find((h) => h.id === habilidadId);
  if (!habilidad) return;
  state.habilidadesPendientes = state.habilidadesPendientes.filter((h) => h.habilidadId !== habilidadId);
  state.habilidadesPendientes.push({ habilidadId, nombre: habilidad.nombre, nivel });
  renderHabilidadesPendientes();
}

function nivelClase(nivel) {
  if (nivel === "dominado") return "nivel-dominado";
  if (nivel === "basico") return "nivel-basico";
  return "nivel-no-tiene";
}

function renderHabilidadesPendientes() {
  const el = $("habilidades-pendientes");
  el.innerHTML = "";
  state.habilidadesPendientes.forEach((h) => {
    const fila = document.createElement("div");
    fila.className = "hab-row";
    fila.innerHTML =
      "<span>" + esc(h.nombre) + " <small class='muted'>(sin guardar)</small></span>" +
      "<span><span class='nivel-tag " + nivelClase(h.nivel) + "'>" + esc(NIVELES[h.nivel] || h.nivel) + "</span>" +
      "<button class='icon-btn' title='Quitar' aria-label='Quitar " + esc(h.nombre) + "'>✕</button></span>";
    fila.querySelector(".icon-btn").onclick = () => {
      state.habilidadesPendientes = state.habilidadesPendientes.filter((x) => x.habilidadId !== h.habilidadId);
      renderHabilidadesPendientes();
    };
    el.appendChild(fila);
  });
}

function olvidarEstudiante() {
  state.estudianteId = null;
  try { localStorage.removeItem("careergps_estudiante_id"); } catch (e) { }
}

async function guardarPerfil() {
  const statusEl = $("perfil-status");
  const carrera = $("input-carrera").value.trim();
  statusEl.className = "status-msg";
  if (!carrera) {
    statusEl.className = "error-msg";
    statusEl.textContent = "Escribe tu carrera para guardar el perfil.";
    return;
  }
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
        olvidarEstudiante();
      }
    }
    if (!state.estudianteId) {
      const estudiante = await fetchJson(API + "/estudiantes", opts("POST"));
      state.estudianteId = estudiante.id;
      try { localStorage.setItem("careergps_estudiante_id", estudiante.id); } catch (e) { }
    }
    for (const h of state.habilidadesPendientes) {
      await fetchJson(API + "/estudiantes/" + state.estudianteId + "/habilidades", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ habilidadId: h.habilidadId, nivel: h.nivel }),
      });
    }
    state.habilidadesPendientes = [];
    renderHabilidadesPendientes();
    statusEl.textContent = "";
    toast("Perfil guardado ✓", "ok");
    await cargarMisHabilidades();
  } catch (e) {
    statusEl.className = "error-msg";
    statusEl.textContent = e.message;
  }
}

async function cargarMiPerfil() {
  if (!state.estudianteId) return;
  try {
    const est = await fetchJson(API + "/estudiantes/" + state.estudianteId);
    if (est && est.carrera) $("input-carrera").value = est.carrera;
  } catch (e) {
    if (e.status === 404) olvidarEstudiante();
  }
}

async function cargarMisHabilidades() {
  const el = $("habilidades-registradas");
  if (!state.estudianteId) {
    el.innerHTML = '<p class="muted">Todavía no registraste tu perfil.</p>';
    return;
  }
  try {
    const habilidades = await fetchJson(API + "/estudiantes/" + state.estudianteId + "/habilidades");
    if (habilidades.length === 0) {
      el.innerHTML = '<p class="muted">Todavía no agregaste habilidades.</p>';
      return;
    }
    el.innerHTML = "";
    habilidades.forEach((h) => {
      const catalogo = state.habilidades.find((c) => c.id === h.habilidadId);
      const fila = document.createElement("div");
      fila.className = "hab-row";
      fila.innerHTML =
        "<span>" + esc(catalogo ? catalogo.nombre : "Habilidad #" + h.habilidadId) + "</span>" +
        "<span class='nivel-tag " + nivelClase(h.nivel) + "'>" + esc(NIVELES[h.nivel] || h.nivel) + "</span>";
      el.appendChild(fila);
    });
  } catch (e) {
    if (e.status === 404) olvidarEstudiante();
    el.innerHTML = '<p class="muted">Todavía no registraste tu perfil.</p>';
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
    resultadoEl.classList.remove("hidden");
  } catch (e) {
    if (e.status === 404 && /estudiante/i.test(e.message)) olvidarEstudiante();
    mostrarError("alineacion-error", e.message);
  }
}

function irAlApp(vista) {
  mostrarVista(vista || "demanda");
  if (vista === "perfil") { cargarMiPerfil(); cargarMisHabilidades(); }
}

document.querySelectorAll(".tab-btn").forEach((btn) => {
  btn.addEventListener("click", () => irAlApp(btn.dataset.view));
});
$("logo").addEventListener("click", () => mostrarVista("landing"));
$("btn-ingresar").addEventListener("click", () => irAlApp("demanda"));
$("btn-registrar-cta").addEventListener("click", () => irAlApp("perfil"));
$("btn-ver-demanda").addEventListener("click", (e) => conCarga(e.currentTarget, verDemanda));
$("btn-ver-relacionados").addEventListener("click", (e) => conCarga(e.currentTarget, () => verRelacionados()));
$("btn-ver-relacionados-desde-demanda").addEventListener("click", () => verRelacionados(state.puestoDemandaId));
$("btn-agregar-habilidad").addEventListener("click", agregarHabilidadPendiente);
$("btn-guardar-perfil").addEventListener("click", (e) => conCarga(e.currentTarget, guardarPerfil));
$("btn-ver-alineacion").addEventListener("click", (e) => conCarga(e.currentTarget, verAlineacion));

(async function init() {
  mostrarVista("landing");
  try {
    await cargarPuestos();
    await cargarHabilidades();
    await cargarMiPerfil();
    await cargarMisHabilidades();
  } catch (e) {
    console.error("Error inicializando CareerGPS:", e);
    toast(e.message, "error");
  }
})();
