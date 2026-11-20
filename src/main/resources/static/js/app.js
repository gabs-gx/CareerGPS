const API = "/api";

const NIVELES = { "dominado": "Dominado", "basico": "Básico", "no tiene": "No tiene" };
const ORDEN_NIVELES = ["basico", "dominado", "no tiene"];
const ICONOS = {
  "Data Analyst": "📊", "Data Scientist": "🧪", "Data Engineer": "🛠️", "BI Analyst": "📈",
  "Backend Developer": "⚙️", "Frontend Developer": "🎨", "DevOps Engineer": "🚀", "QA Analyst": "🔍",
};
const CATEGORIAS = {
  "Datos": ["SQL", "Excel", "Power BI", "Tableau", "Estadistica", "Machine Learning", "Spark"],
  "Programación": ["Python", "Java", "Spring Boot", "JavaScript", "HTML/CSS", "React", "Node.js"],
  "Nube y DevOps": ["Git", "Docker", "Kubernetes", "AWS", "Linux", "CI/CD"],
  "Calidad y diseño": ["Selenium", "Figma"],
};
const VISTAS_LIBRES = ["landing", "perfil", "login"];

const state = {
  puestos: [],
  habilidades: [],
  demandas: {},
  nivelesUI: {},
  nivelesGuardados: {},
  puestoDemandaId: null,
  puestoRelacionadosId: null,
  puestoAlineacionId: null,
  estudianteId: null,
  nombres: null,
  apellidos: null,
  correo: null,
  carrera: null,
  paso: 1,
  categoria: "Todas",
  busqueda: "",
};

try {
  const guardada = JSON.parse(localStorage.getItem("careergps_sesion") || "null");
  if (guardada && guardada.id) {
    state.estudianteId = String(guardada.id);
    state.nombres = guardada.nombres;
    state.apellidos = guardada.apellidos;
    state.correo = guardada.correo;
    state.carrera = guardada.carrera;
  }
} catch (e) { }

const $ = (id) => document.getElementById(id);
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

function esc(texto) {
  const d = document.createElement("div");
  d.textContent = String(texto);
  return d.innerHTML;
}

function nombreCompleto() {
  return [state.nombres, state.apellidos].filter(Boolean).join(" ");
}

function guardarSesion() {
  try {
    localStorage.setItem("careergps_sesion", JSON.stringify({
      id: state.estudianteId, nombres: state.nombres, apellidos: state.apellidos,
      correo: state.correo, carrera: state.carrera,
    }));
  } catch (e) { }
}

function aplicarSesion(s) {
  state.estudianteId = String(s.id);
  state.nombres = s.nombres;
  state.apellidos = s.apellidos;
  state.correo = s.correo;
  state.carrera = s.carrera;
  guardarSesion();
}

function sesionActiva() {
  return !!state.estudianteId;
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

function mostrarError(id, mensaje) {
  const el = $(id);
  el.textContent = mensaje;
  el.classList.remove("hidden");
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
  vista = vista || "inicio";
  if (!sesionActiva() && !VISTAS_LIBRES.includes(vista)) {
    toast("Primero crea tu perfil para continuar", "error");
    vista = "perfil";
  }
  mostrarVista(vista);
  if (vista === "perfil") { irPaso(1); cargarMiPerfil(); }
  if (vista === "inicio") renderInicio();
}

/* ---------- datos del mercado ---------- */

function categoriaDe(nombre) {
  for (const [cat, lista] of Object.entries(CATEGORIAS)) if (lista.includes(nombre)) return cat;
  return "Otras";
}

function puestosQuePiden(nombre) {
  return Object.values(state.demandas).filter((d) => d.habilidades.some((h) => h.nombre === nombre)).length;
}

async function cargarDemandas() {
  await Promise.all(state.puestos.filter((p) => p.disponible).map(async (p) => {
    try { state.demandas[p.id] = await fetchJson(API + "/puestos/" + p.id + "/demanda"); } catch (e) { }
  }));
}

function idsPropios() {
  return new Set(Object.entries(state.nivelesGuardados)
    .filter(([, nivel]) => nivel !== "no tiene")
    .map(([id]) => Number(id)));
}

function coincidenciaCon(puestoId) {
  const propios = idsPropios();
  const habilidades = state.demandas[puestoId].habilidades;
  const coincide = habilidades.filter((h) => propios.has(h.habilidadId));
  const faltan = habilidades.filter((h) => !propios.has(h.habilidadId));
  return { pct: habilidades.length ? Math.round(coincide.length * 100 / habilidades.length) : 0, coincide, faltan };
}

/* ---------- alertas ---------- */

const MAX_ALERTAS = 30;
const ICONOS_ALERTA = { sube: "📈", baja: "📉", mercado: "🆕", meta: "🎯", info: "👋" };

const claveAlertas = () => "careergps_alertas_" + state.estudianteId;
const claveSnap = () => "careergps_snap_" + state.estudianteId;

function leerLS(clave, defecto) {
  try {
    const valor = JSON.parse(localStorage.getItem(clave) || "null");
    return valor === null ? defecto : valor;
  } catch (e) { return defecto; }
}

function escribirLS(clave, valor) {
  try { localStorage.setItem(clave, JSON.stringify(valor)); } catch (e) { }
}

function tiempoRelativo(ts) {
  const s = Math.floor((Date.now() - ts) / 1000);
  if (s < 60) return "hace un momento";
  const m = Math.floor(s / 60);
  if (m < 60) return "hace " + m + " min";
  const h = Math.floor(m / 60);
  return h < 24 ? "hace " + h + " h" : "hace " + Math.floor(h / 24) + " d";
}

function renderAlertas() {
  const lista = sesionActiva() ? leerLS(claveAlertas(), []) : [];
  const sinLeer = lista.filter((a) => !a.leida).length;
  $("bell-badge").textContent = sinLeer;
  $("bell-badge").classList.toggle("hidden", sinLeer === 0);
  const cont = $("alert-lista");
  cont.innerHTML = lista.length ? "" : '<p class="muted alert-vacia">Aún no tienes alertas.</p>';
  lista.forEach((a) => {
    const item = document.createElement("div");
    item.className = "alert-item" + (a.leida ? "" : " nueva");
    item.innerHTML = '<span class="alert-ico">' + (ICONOS_ALERTA[a.tipo] || "🔔") + '</span><div><p>' + esc(a.texto) +
      '</p><small>' + tiempoRelativo(a.fecha) + '</small></div>';
    cont.appendChild(item);
  });
}

function evaluarAlertas() {
  if (!sesionActiva() || !Object.keys(state.demandas).length) return;
  const previo = leerLS(claveSnap(), null);
  const actual = {};
  const nuevas = [];

  const subidas = [];
  const bajadas = [];
  state.puestos.filter((p) => state.demandas[p.id]).forEach((p) => {
    const c = coincidenciaCon(p.id);
    const demandadas = state.demandas[p.id].habilidades.map((h) => h.nombre);
    actual[p.id] = { pct: c.pct, demandadas };
    const antes = previo && previo[p.id];
    if (!antes) return;

    const nuevasHab = demandadas.filter((n) => !antes.demandadas.includes(n));
    const cambio = { p, antes: antes.pct, ahora: c.pct, nuevasHab };
    if (c.pct > antes.pct) subidas.push(cambio);
    else if (c.pct < antes.pct) bajadas.push(cambio);
    if (nuevasHab.length && c.pct >= antes.pct) {
      nuevas.push(["mercado", p.nombre + " ahora también pide " + nuevasHab.join(", ") + "."]);
    }
    [50, 80].forEach((meta) => {
      if (antes.pct < meta && c.pct >= meta) {
        nuevas.push(["meta", "¡Tu perfil ya coincide con el " + meta + "% o más de lo que piden las ofertas de " + p.nombre + "!"]);
      }
    });
  });

  const detalle = (x) => x.p.nombre + " (" + x.antes + "% → " + x.ahora + "%)";
  const resumir = (lista, tipo, verbo) => {
    if (!lista.length) return;
    if (lista.length <= 2) {
      lista.forEach((x) => nuevas.push([tipo, "Tu alineación con " + x.p.nombre + " " + verbo + " de " + x.antes + "% a " + x.ahora + "%." +
        (tipo === "baja" && x.nuevasHab.length ? " Ahora también piden " + x.nuevasHab.join(", ") + "." : "")]));
      return;
    }
    const orden = lista.slice().sort((a, b) => Math.abs(b.ahora - b.antes) - Math.abs(a.ahora - a.antes));
    nuevas.push([tipo, "Tu alineación " + verbo + " en " + lista.length + " puestos. Lo más notable: " + detalle(orden[0]) + " y " + detalle(orden[1]) + "."]);
  };
  resumir(subidas, "sube", "subió");
  resumir(bajadas, "baja", "bajó");

  if (!previo) nuevas.push(["info", "Te avisaremos aquí cuando cambie tu alineación o lo que pide el mercado."]);
  escribirLS(claveSnap(), actual);

  const guardadas = leerLS(claveAlertas(), []);
  nuevas.forEach(([tipo, texto]) => guardadas.unshift({ tipo, texto, fecha: Date.now(), leida: false }));
  escribirLS(claveAlertas(), guardadas.slice(0, MAX_ALERTAS));
  renderAlertas();
  if (nuevas.length) toast("🔔 Tienes " + nuevas.length + (nuevas.length === 1 ? " alerta nueva" : " alertas nuevas"));
}

/* ---------- sesión ---------- */

function actualizarSesionUI() {
  const on = sesionActiva();
  document.querySelectorAll(".tab-btn").forEach((b) => {
    b.classList.toggle("locked", !on && !VISTAS_LIBRES.includes(b.dataset.view));
  });
  document.querySelectorAll(".feature-card").forEach((c) => c.classList.toggle("locked", !on));
  $("btn-ingresar").classList.toggle("hidden", on);
  $("user-wrap").classList.toggle("hidden", !on);
  $("alert-wrap").classList.toggle("hidden", !on);
  renderAlertas();
  $("btn-tengo-cuenta").classList.toggle("hidden", on);
  $("btn-registrar-cta").textContent = on ? "Ver mis coincidencias ›" : "Crear mi perfil ›";
  $("perfil-titulo").textContent = on ? "Mi perfil" : "Crea tu perfil";
  $("perfil-subtitulo").textContent = on
    ? "Tus datos de cuenta. Solo la carrera se puede cambiar."
    : "Lo necesitamos para comparar tus habilidades con lo que pide el mercado.";
  $("btn-guardar-perfil").textContent = on ? "Guardar cambios" : "Crear mi perfil y entrar ›";
  $("user-badge").textContent = state.nombres ? state.nombres.trim()[0].toUpperCase() : "👤";
  $("menu-nombre").textContent = nombreCompleto() || "Mi cuenta";
  $("menu-carrera").textContent = state.carrera || "";
  $("menu-codigo").textContent = state.correo || "";
  ["input-nombres", "input-apellidos", "input-correo"].forEach((id) => { $(id).disabled = on; });
  $("campo-password").classList.toggle("hidden", on);
  actualizarPasos();
}

function olvidarEstudiante() {
  state.estudianteId = null;
  state.nombres = null;
  state.apellidos = null;
  state.correo = null;
  state.carrera = null;
  state.nivelesGuardados = {};
  state.nivelesUI = {};
  try { localStorage.removeItem("careergps_sesion"); } catch (e) { }
  actualizarSesionUI();
}

function cerrarSesion() {
  olvidarEstudiante();
  ["input-nombres", "input-apellidos", "input-correo", "input-password", "input-carrera"].forEach((id) => { $(id).value = ""; });
  $("user-menu").classList.add("hidden");
  irPaso(1);
  renderChips();
  toast("Sesión cerrada");
  mostrarVista("landing");
}

async function ingresar() {
  const errorEl = $("login-error");
  errorEl.classList.add("hidden");
  const correo = $("input-login-correo").value.trim();
  const password = $("input-login-password").value;
  if (!correo || !password) return mostrarError("login-error", "Escribe tu correo y tu contraseña.");
  const btn = $("btn-login");
  btn.disabled = true;
  try {
    const sesion = await fetchJson(API + "/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ correo, password }),
    });
    aplicarSesion(sesion);
    $("input-login-password").value = "";
    await cargarMiPerfil();
    if (!Object.keys(state.demandas).length) await cargarDemandas();
    evaluarAlertas();
    toast("¡Qué bueno verte de nuevo!", "ok");
    irAlApp("inicio");
  } catch (e) {
    mostrarError("login-error", e.message);
  } finally {
    btn.disabled = false;
  }
}

function mostrarBienvenida() {
  $("modal-titulo").textContent = "¡Listo, " + (state.nombres || "bienvenido/a") + "!";
  $("modal-codigo").textContent = state.correo || "";
  $("modal-bienvenida").classList.remove("hidden");
}

/* ---------- asistente de perfil ---------- */

function datosCompletos() {
  const v = (id) => $(id).value.trim();
  const correoOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v("input-correo"));
  return v("input-nombres") !== "" && v("input-apellidos") !== "" && v("input-carrera") !== "" &&
    (sesionActiva() || (correoOk && $("input-password").value.length >= 8));
}

function irPaso(n) {
  state.paso = n;
  $("panel-datos").classList.toggle("hidden", n !== 1);
  $("panel-habilidades").classList.toggle("hidden", n !== 2);
  if (n === 2) renderChips();
  actualizarPasos();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function actualizarPasos() {
  const datos = datosCompletos();
  const habilidades = Object.keys(state.nivelesUI).length > 0;
  const pasos = document.querySelectorAll(".paso");
  pasos[0].classList.toggle("hecho", state.paso > 1 && datos);
  pasos[0].classList.toggle("actual", state.paso === 1);
  pasos[1].classList.toggle("actual", state.paso === 2 && !(datos && habilidades));
  pasos[1].classList.toggle("hecho", state.paso === 2 && datos && habilidades);
  pasos[2].classList.toggle("actual", state.paso === 2 && datos && habilidades);
  $("btn-siguiente").disabled = !datos;
  $("btn-guardar-perfil").disabled = !(datos && (sesionActiva() || habilidades));
}

function renderCategorias() {
  const el = $("filtro-categorias");
  el.innerHTML = "";
  ["Todas", ...Object.keys(CATEGORIAS)].forEach((cat) => {
    const b = document.createElement("button");
    b.type = "button";
    b.className = "cat-pill" + (state.categoria === cat ? " activa" : "");
    b.textContent = cat;
    b.onclick = () => { state.categoria = cat; renderCategorias(); renderCatalogo(); };
    el.appendChild(b);
  });
}

function renderCatalogo() {
  const el = $("catalogo");
  const texto = state.busqueda.trim().toLowerCase();
  const lista = state.habilidades
    .filter((h) => state.categoria === "Todas" || categoriaDe(h.nombre) === state.categoria)
    .filter((h) => !texto || h.nombre.toLowerCase().includes(texto))
    .sort((a, b) => puestosQuePiden(b.nombre) - puestosQuePiden(a.nombre) || a.nombre.localeCompare(b.nombre));
  el.innerHTML = lista.length ? "" : '<p class="muted">No encontramos habilidades con ese nombre.</p>';
  lista.forEach((h) => {
    const n = puestosQuePiden(h.nombre);
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "skill-chip" + (state.nivelesUI[h.id] ? " sel" : "");
    btn.innerHTML = "<span>" + (state.nivelesUI[h.id] ? "✓ " : "") + esc(h.nombre) + "</span>" +
      (n ? "<small>📈 " + n + (n === 1 ? " puesto" : " puestos") + "</small>" : "");
    btn.onclick = () => {
      if (state.nivelesUI[h.id]) {
        if (state.nivelesGuardados[h.id]) return toast("Esa habilidad ya está guardada. Cambia su nivel a la derecha.");
        delete state.nivelesUI[h.id];
      } else {
        state.nivelesUI[h.id] = "basico";
      }
      renderChips();
    };
    el.appendChild(btn);
  });
}

function nivelClase(nivel) {
  if (nivel === "dominado") return "nivel-dominado";
  if (nivel === "basico") return "nivel-basico";
  return "nivel-no-tiene";
}

function renderSeleccion() {
  const ids = Object.keys(state.nivelesUI);
  $("seleccion-titulo").textContent = "Mis habilidades (" + ids.length + ")";
  const el = $("seleccion");
  el.innerHTML = ids.length ? "" : '<p class="muted">Aún no elegiste ninguna. Toca las del catálogo.</p>';
  ids.forEach((id) => {
    const hab = state.habilidades.find((h) => String(h.id) === id);
    const fila = document.createElement("div");
    fila.className = "sel-fila";
    fila.innerHTML = '<div class="sel-top"><span>' + esc(hab ? hab.nombre : "Habilidad " + id) + "</span></div>";
    if (!state.nivelesGuardados[id]) {
      const quitar = document.createElement("button");
      quitar.type = "button";
      quitar.className = "icon-btn";
      quitar.title = "Quitar";
      quitar.textContent = "✕";
      quitar.onclick = () => { delete state.nivelesUI[id]; renderChips(); };
      fila.querySelector(".sel-top").appendChild(quitar);
    }
    const seg = document.createElement("div");
    seg.className = "segmentado";
    ORDEN_NIVELES.forEach((nivel) => {
      const b = document.createElement("button");
      b.type = "button";
      b.textContent = NIVELES[nivel];
      b.className = state.nivelesUI[id] === nivel ? "on " + nivelClase(nivel) : "";
      b.onclick = () => { state.nivelesUI[id] = nivel; renderChips(); };
      seg.appendChild(b);
    });
    fila.appendChild(seg);
    el.appendChild(fila);
  });
}

function renderFuerza() {
  const ranking = state.habilidades
    .map((h) => ({ h, n: puestosQuePiden(h.nombre) }))
    .filter((x) => x.n > 0)
    .sort((a, b) => b.n - a.n)
    .slice(0, 8);
  const total = ranking.length;
  const tengo = ranking.filter((x) => state.nivelesUI[x.h.id] && state.nivelesUI[x.h.id] !== "no tiene").length;
  $("fuerza-num").textContent = tengo + "/" + total;
  $("fuerza-barra").style.width = (total ? tengo * 100 / total : 0) + "%";
  $("fuerza-texto").textContent = total
    ? (tengo === 0
      ? "Estas son las " + total + " habilidades más pedidas del mercado. Marca las que ya tienes."
      : "Tienes " + tengo + " de las " + total + " habilidades más pedidas del mercado.")
    : "";
}

function renderChips() {
  renderCategorias();
  renderCatalogo();
  renderSeleccion();
  renderFuerza();
  actualizarPasos();
}

async function cargarMiPerfil() {
  if (!state.estudianteId) { renderChips(); actualizarSesionUI(); return; }
  try {
    const est = await fetchJson(API + "/estudiantes/" + state.estudianteId);
    if (est && est.carrera) { $("input-carrera").value = est.carrera; state.carrera = est.carrera; }
    $("input-nombres").value = state.nombres || "";
    $("input-apellidos").value = state.apellidos || "";
    $("input-correo").value = state.correo || "";
    const lista = await fetchJson(API + "/estudiantes/" + state.estudianteId + "/habilidades");
    state.nivelesGuardados = {};
    lista.forEach((h) => { state.nivelesGuardados[h.habilidadId] = h.nivel; });
    state.nivelesUI = Object.assign({}, state.nivelesGuardados);
  } catch (e) {
    if (e.status === 404) olvidarEstudiante();
  }
  renderChips();
  actualizarSesionUI();
}

async function guardarPerfil() {
  const btn = $("btn-guardar-perfil");
  const statusEl = $("perfil-status");
  const v = (id) => $(id).value.trim();
  const carrera = v("input-carrera");
  const eraNuevo = !sesionActiva();
  statusEl.className = "status-msg";
  statusEl.textContent = "";
  if (!datosCompletos()) {
    statusEl.className = "error-msg";
    statusEl.textContent = "Completa todos tus datos para continuar.";
    return irPaso(1);
  }
  btn.disabled = true;
  btn.textContent = "Guardando…";
  try {
    const json = (method, cuerpo) => ({
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(cuerpo),
    });
    if (eraNuevo) {
      const sesion = await fetchJson(API + "/auth/registro", json("POST", {
        nombres: v("input-nombres"), apellidos: v("input-apellidos"), correo: v("input-correo"),
        password: $("input-password").value, carrera,
      }));
      aplicarSesion(sesion);
      $("input-password").value = "";
    } else {
      try {
        await fetchJson(API + "/estudiantes/" + state.estudianteId, json("PUT", { carrera }));
      } catch (e) {
        if (e.status === 404) {
          olvidarEstudiante();
          throw new Error("Tu cuenta ya no existe. Crea una nueva.");
        }
        throw e;
      }
      state.carrera = carrera;
      guardarSesion();
    }
    for (const id of Object.keys(state.nivelesUI).filter((k) => state.nivelesUI[k] !== state.nivelesGuardados[k])) {
      await fetchJson(API + "/estudiantes/" + state.estudianteId + "/habilidades",
        json("POST", { habilidadId: Number(id), nivel: state.nivelesUI[id] }));
    }
    state.nivelesGuardados = Object.assign({}, state.nivelesUI);
    actualizarSesionUI();
    renderChips();
    evaluarAlertas();
    if (eraNuevo) mostrarBienvenida();
    else { toast("Perfil guardado ✓", "ok"); irAlApp("inicio"); }
  } catch (e) {
    statusEl.className = "error-msg";
    statusEl.textContent = e.message;
  } finally {
    btn.disabled = false;
    actualizarSesionUI();
  }
}

/* ---------- para ti ---------- */

function renderInicio() {
  if (!sesionActiva()) return;
  $("inicio-saludo").textContent = "Hola, " + (state.nombres || "") + " 👋";
  const vacio = idsPropios().size === 0;
  $("inicio-vacio").classList.toggle("hidden", !vacio);
  $("inicio-contenido").classList.toggle("hidden", vacio);
  if (vacio) return;

  const ranking = state.puestos
    .filter((p) => state.demandas[p.id])
    .map((p) => Object.assign({ p }, coincidenciaCon(p.id)))
    .sort((a, b) => b.pct - a.pct);

  const lista = $("inicio-ranking");
  lista.innerHTML = ranking.length ? "" : '<p class="muted">Cargando datos del mercado…</p>';
  ranking.forEach((r, i) => {
    const fila = document.createElement("button");
    fila.className = "rank-row";
    fila.innerHTML =
      '<div class="rank-top"><span><span class="rank-pos">' + (i + 1) + '</span>' + (ICONOS[r.p.nombre] || "💼") + " " + esc(r.p.nombre) +
      '</span><strong>' + r.pct + '% ›</strong></div><div class="barra-track"><div class="barra-fill' + (i % 2 ? " c-lime" : "") + '" style="width:0%"></div></div>';
    fila.onclick = () => {
      state.puestoAlineacionId = r.p.id;
      refrescarPuestos();
      mostrarVista("alineacion");
      verAlineacion();
    };
    lista.appendChild(fila);
    requestAnimationFrame(() => requestAnimationFrame(() => { fila.querySelector(".barra-fill").style.width = r.pct + "%"; }));
  });

  const acumulado = {};
  ranking.slice(0, 3).forEach((r) => r.faltan.forEach((f) => {
    const a = acumulado[f.nombre] || (acumulado[f.nombre] = { nombre: f.nombre, total: 0, puestos: 0 });
    a.total += f.porcentaje;
    a.puestos++;
  }));
  const top = Object.values(acumulado).sort((a, b) => b.total - a.total).slice(0, 3);
  const aprender = $("inicio-aprender");
  aprender.innerHTML = top.length ? "" : '<p class="muted">¡Ya cubres lo que piden tus mejores puestos!</p>';
  top.forEach((t, i) => {
    const card = document.createElement("div");
    card.className = "aprender-card";
    card.innerHTML = '<span class="num">PRIORIDAD ' + (i + 1) + '</span><h4>' + esc(t.nombre) + '</h4><p>Se pide en ' + t.puestos +
      (t.puestos === 1 ? " de tus 3 mejores puestos" : " de tus 3 mejores puestos") + ', en promedio en el ' + Math.round(t.total / t.puestos) + '% de sus ofertas.</p>';
    aprender.appendChild(card);
  });
}

/* ---------- explorar ---------- */

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
    animarNumero(el, Number(el.dataset.n), (x) => String(x), 900);
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
    animarNumero($("demanda-total"), demanda.totalOfertas, (x) => x + " ofertas analizadas", 700);
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
        animarNumero(f.fila.querySelector("strong"), Math.round(f.porcentaje), (x) => x + "%", 700);
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

async function verAlineacion() {
  const errorEl = $("alineacion-error");
  const resultadoEl = $("alineacion-resultado");
  errorEl.classList.add("hidden");
  resultadoEl.classList.add("hidden");

  if (!state.estudianteId) {
    return mostrarError("alineacion-error", "Primero crea tu perfil.");
  }
  if (!state.puestoAlineacionId) return mostrarError("alineacion-error", "Elige un puesto para comparar.");
  try {
    const a = await fetchJson(API + "/estudiantes/" + state.estudianteId + "/alineacion/" + state.puestoAlineacionId);
    $("alineacion-titulo").textContent = "Tu alineación con " + a.puestoNombre;
    const demanda = state.demandas[state.puestoAlineacionId];
    const porcentaje = (nombre) => {
      const h = demanda && demanda.habilidades.find((x) => x.nombre === nombre);
      return h ? " · " + Math.round(h.porcentaje) + "%" : "";
    };
    const chips = (lista, vacio, extra) => lista.length
      ? lista.map((c) => "<span class='chip'>" + esc(c) + (extra ? porcentaje(c) : "") + "</span>").join("")
      : "<span class='muted'>" + vacio + "</span>";
    $("alineacion-coincidencias").innerHTML = chips(a.coincidencias, "Ninguna todavía", false);
    $("alineacion-fortalecer").innerHTML = chips(a.aFortalecer, "¡Dominas todo lo que pide este puesto!", true);

    const total = a.coincidencias.length + a.aFortalecer.length;
    const pct = total ? Math.round(a.coincidencias.length * 100 / total) : 0;
    $("ring-texto").textContent = total
      ? "Cubres " + a.coincidencias.length + " de las " + total + " habilidades que pide este puesto."
      : "Este puesto todavía no tiene habilidades registradas.";
    const ring = $("ring");
    ring.style.setProperty("--p", 0);
    resultadoEl.classList.remove("hidden");
    animarNumero($("ring-num"), pct, (x) => x + "%", 900, (x) => ring.style.setProperty("--p", x));
  } catch (e) {
    if (e.status === 404 && /estudiante/i.test(e.message)) olvidarEstudiante();
    mostrarError("alineacion-error", e.message);
  }
}

/* ---------- eventos ---------- */

document.querySelectorAll(".tab-btn").forEach((btn) => {
  btn.addEventListener("click", () => irAlApp(btn.dataset.view));
});
document.querySelectorAll("[data-go]").forEach((card) => {
  const ir = () => irAlApp(card.dataset.go);
  card.addEventListener("click", ir);
  card.addEventListener("keydown", (e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); ir(); } });
});
$("logo").addEventListener("click", () => mostrarVista("landing"));
$("btn-ingresar").addEventListener("click", () => irAlApp("login"));
$("btn-tengo-cuenta").addEventListener("click", () => irAlApp("login"));
$("btn-ir-crear").addEventListener("click", () => irAlApp("perfil"));
$("btn-registrar-cta").addEventListener("click", () => irAlApp(sesionActiva() ? "inicio" : "perfil"));
$("btn-login").addEventListener("click", ingresar);
["input-login-correo", "input-login-password"].forEach((id) => {
  $(id).addEventListener("keydown", (e) => { if (e.key === "Enter") ingresar(); });
});
["input-nombres", "input-apellidos", "input-correo", "input-password", "input-carrera"].forEach((id) => {
  $(id).addEventListener("input", actualizarPasos);
});
$("btn-ver-password").addEventListener("click", () => {
  const campo = $("input-password");
  const oculto = campo.type === "password";
  campo.type = oculto ? "text" : "password";
  $("btn-ver-password").textContent = oculto ? "Ocultar contraseña" : "Mostrar contraseña";
});
$("btn-siguiente").addEventListener("click", () => irPaso(2));
$("btn-atras").addEventListener("click", () => irPaso(1));
document.querySelectorAll("button.paso").forEach((p) => {
  p.addEventListener("click", () => {
    const destino = Number(p.dataset.paso);
    if (destino === 2 && !datosCompletos()) return toast("Completa tus datos primero", "error");
    irPaso(destino);
  });
});
$("buscar-habilidad").addEventListener("input", (e) => { state.busqueda = e.target.value; renderCatalogo(); });
$("btn-inicio-habilidades").addEventListener("click", () => { irAlApp("perfil"); if (datosCompletos()) irPaso(2); });
$("btn-logout").addEventListener("click", cerrarSesion);
$("user-badge").addEventListener("click", (e) => {
  e.stopPropagation();
  $("alert-menu").classList.add("hidden");
  $("user-menu").classList.toggle("hidden");
});
$("btn-alertas").addEventListener("click", (e) => {
  e.stopPropagation();
  $("user-menu").classList.add("hidden");
  const menu = $("alert-menu");
  const abrir = menu.classList.contains("hidden");
  menu.classList.toggle("hidden");
  if (abrir) {
    renderAlertas();
    const lista = leerLS(claveAlertas(), []);
    lista.forEach((a) => { a.leida = true; });
    escribirLS(claveAlertas(), lista);
    $("bell-badge").classList.add("hidden");
  }
});
$("btn-limpiar-alertas").addEventListener("click", () => { escribirLS(claveAlertas(), []); renderAlertas(); });
document.addEventListener("click", () => {
  $("user-menu").classList.add("hidden");
  $("alert-menu").classList.add("hidden");
});
$("user-menu").addEventListener("click", (e) => e.stopPropagation());
$("alert-menu").addEventListener("click", (e) => e.stopPropagation());
$("btn-modal-ok").addEventListener("click", () => {
  $("modal-bienvenida").classList.add("hidden");
  irAlApp("inicio");
});
$("btn-ver-relacionados-desde-demanda").addEventListener("click", () => verRelacionados(state.puestoDemandaId));
$("btn-guardar-perfil").addEventListener("click", guardarPerfil);

(async function init() {
  mostrarVista("landing");
  actualizarSesionUI();
  esqueleto();
  try {
    state.puestos = await conReintentos(API + "/puestos/resumen");
    state.habilidades = await conReintentos(API + "/habilidades");
    refrescarPuestos();
    renderStats();
    const tenia = sesionActiva();
    await cargarMiPerfil();
    if (tenia && sesionActiva()) irAlApp("inicio");
    cargarDemandas().then(() => {
      evaluarAlertas();
      renderChips();
      if ($("view-inicio").classList.contains("active")) renderInicio();
    });
  } catch (e) {
    console.error("Error inicializando CareerGPS:", e);
    toast(e.message, "error");
  }
})();
