if (document.getElementById('loginForm')) {
document.getElementById('loginForm').addEventListener('submit', async event => {
    event.preventDefault();
    const button = event.submitter;
    const error = document.getElementById('errorMsg');
    error.textContent = ''; button.disabled = true;
    try {
        const data = await apiFetch('/auth/login', {
            method: 'POST', body: JSON.stringify({
                username: document.getElementById('username').value.trim(),
                password: document.getElementById('password').value
            })
        }, false);
        sessionStorage.setItem('jwt_token', data.token);
        location.href = 'dashboard.html';
    } catch (ex) { error.textContent = ex.message; }
    finally { button.disabled = false; }
});
}
let envios = [], bitacoras = [], flota = [];
const $ = id => document.getElementById(id);
const money = new Intl.NumberFormat('es-CR', { style: 'currency', currency: 'CRC' });
function mensaje(texto, error = false) {
    $('statusMsg').textContent = texto; $('statusMsg').hidden = !texto;
    $('statusMsg').className = error ? 'notice error' : 'notice';
}
function texto(tag, value, className) {
    const element = document.createElement(tag); element.textContent = value;
    if (className) element.className = className; return element;
}
function boton(label, action, className = 'btn-info') {
    const b = texto('button', label, className); b.type = 'button';
    b.addEventListener('click', async () => {
        b.disabled = true;
        try { await action(); } catch (ex) { mensaje(ex.message, true); }
        finally { b.disabled = false; }
    }); return b;
}
async function cargarEnvios() {
    $('enviosGrid').textContent = 'Cargando envíos…';
    try { envios = await fetchWithAuth('/envios'); renderEnvios(); actualizarKpi(); }
    catch (ex) { $('enviosGrid').textContent = 'No se pudieron cargar los envíos.'; mensaje(ex.message, true); }
}
function renderEnvios() {
    const container = $('enviosGrid'); container.replaceChildren();
    const query = $('buscarEnvio').value.toLocaleLowerCase();
    const visibles = envios.filter(e => (!$('estadoFiltro').value || e.estadoEnvio === $('estadoFiltro').value)
        && `${e.codigoRastreo} ${e.direccionDestino}`.toLocaleLowerCase().includes(query));
    if (!visibles.length) { container.textContent = 'No hay envíos para mostrar con estos filtros.'; return; }
    visibles.forEach(e => {
        const card = document.createElement('article'); card.className = 'envio-card';
        card.append(texto('h3', e.codigoRastreo), texto('span', e.estadoEnvio, `badge ${e.estadoEnvio}`),
            texto('p', `Destino: ${e.direccionDestino}`), texto('p', `Peso: ${e.pesoKg} kg · Costo: ${money.format(e.costo)}`),
            texto('p', `Vehículo: ${e.placaVehiculo}`), texto('p', `Conductor: ${e.nombreConductor}`));
        const actions = document.createElement('div'); actions.className = 'card-actions';
        if (tieneRol('ADMIN')) actions.append(boton('Ver bitácora', () => verBitacora(e.id)));
        if (tieneRol('ADMIN', 'OPERADOR', 'CONDUCTOR')) {
            if (e.estadoEnvio === 'PENDIENTE' && tieneRol('ADMIN', 'OPERADOR')) actions.append(boton('Asignar vehículo', () => asignarVehiculo(e.id)));
            if (e.estadoEnvio === 'PENDIENTE' && tieneRol('ADMIN', 'OPERADOR')) actions.append(boton('Marcar en tránsito', () => cambiarEstado(e.id, 'EN_TRANSITO'), 'btn-warning'));
            if (e.estadoEnvio === 'EN_TRANSITO' && tieneRol('ADMIN', 'CONDUCTOR')) actions.append(boton('Marcar entregado', () => cambiarEstado(e.id, 'ENTREGADO'), 'btn-success'));
            if (tieneRol('ADMIN') && ['PENDIENTE', 'EN_TRANSITO'].includes(e.estadoEnvio)) actions.append(boton('Cancelar envío', () => cambiarEstado(e.id, 'CANCELADO'), 'btn-danger'));
        }
        card.append(actions); container.append(card);
    });
}
function actualizarKpi() {
    $('totalEnvios').textContent = envios.length;
    $('paquetesEntregados').textContent = envios.filter(e => e.estadoEnvio === 'ENTREGADO').length;
}
async function asignarVehiculo(id) {
    const vehiculos = await fetchWithAuth('/catalogos/vehiculos');
    const envio = envios.find(e => e.id === id);
    opciones('asignarVehiculoId', vehiculos.filter(v => v.estado !== 'MANTENIMIENTO' && v.capacidadKg >= envio.pesoKg), v => v.placa);
    const dialog = $('asignacionDialog'); dialog.returnValue = '';
    const confirmado = new Promise(resolve => dialog.addEventListener('close', () => resolve(dialog.returnValue === 'confirm'), { once: true }));
    dialog.showModal();
    if (!await confirmado) return;
    await fetchWithAuth(`/envios/${id}/vehiculo`, { method: 'PUT', body: JSON.stringify({ vehiculoId: Number($('asignarVehiculoId').value) }) });
    mensaje('Vehículo asignado.'); await cargarEnvios();
}
function confirmarAccion(titulo, conObservaciones = false) {
    const dialog = $('actionDialog');
    $('actionTitle').textContent = titulo; $('actionNotesGroup').hidden = !conObservaciones;
    $('actionNotes').value = ''; dialog.returnValue = '';
    return new Promise(resolve => {
        dialog.addEventListener('close', () => resolve(dialog.returnValue === 'confirm' ? $('actionNotes').value : null), { once: true });
        dialog.showModal();
    });
}
async function cambiarEstado(id, nuevoEstado) {
    const observaciones = await confirmarAccion(`Cambiar envío a ${nuevoEstado}`, true);
    if (observaciones === null) return;
    if (observaciones.length > 250) throw new Error('La observación no puede superar 250 caracteres.');
    await fetchWithAuth(`/envios/${id}/estado`, { method: 'PUT', body: JSON.stringify({ nuevoEstado, observaciones }) });
    mensaje('Estado actualizado y cambio registrado en la bitácora.'); await cargarEnvios();
}
async function verBitacora(id) {
    bitacoras = await fetchWithAuth(`/envios/${id}/bitacora`);
    $('fechaDesde').value = ''; $('fechaHasta').value = ''; renderBitacora();
    $('bitacoraTitle').textContent = `Bitácora del envío ${envios.find(e => e.id === id).codigoRastreo}`;
    $('bitacoraTitle').focus();
}
function renderBitacora() {
    const content = $('bitacoraContent'); content.replaceChildren();
    const desde = $('fechaDesde').value, hasta = $('fechaHasta').value;
    if (desde && hasta && desde > hasta) { content.textContent = 'La fecha inicial no puede ser posterior a la final.'; return; }
    const rows = bitacoras.filter(b => (!desde || b.fechaCambio.slice(0, 10) >= desde) && (!hasta || b.fechaCambio.slice(0, 10) <= hasta));
    if (!rows.length) { content.textContent = 'No hay registros de auditoría en este rango.'; return; }
    rows.forEach(b => {
        const row = document.createElement('article'); row.className = 'audit-entry';
        row.append(texto('h3', `${b.estadoAnterior} → ${b.estadoNuevo}`),
            texto('p', new Date(b.fechaCambio).toLocaleString('es-CR')),
            texto('p', `Usuario: ${b.usuario}`), texto('p', b.observaciones || 'Sin observaciones'));
        content.append(row);
    });
}
function opciones(id, values, label) {
    const select = $(id), previous = select.value; select.replaceChildren(new Option('Seleccione…', ''));
    values.forEach(item => select.add(new Option(label(item), item.id))); select.value = previous;
}
async function cargarCatalogos() {
    const [vehiculos, conductores, empresas] = await Promise.all([
        fetchWithAuth('/catalogos/vehiculos'), fetchWithAuth('/catalogos/conductores'), fetchWithAuth('/catalogos/empresas')]);
    opciones('vehiculoId', vehiculos.filter(v => v.estado !== 'MANTENIMIENTO'), v => `${v.placa} · ${v.capacidadKg} kg`);
    opciones('conductorId', conductores, c => c.nombre); opciones('empresaId', empresas, e => e.nombre);
    $('envioForm').querySelector('[type="submit"]').disabled = !vehiculos.some(v => v.estado !== 'MANTENIMIENTO') || !conductores.length;
    if (!vehiculos.length || !conductores.length) mensaje('Faltan vehículos o conductores. Registre la flota o cargue los datos iniciales de la base de datos.', true);
}
async function cargarFlota() {
    flota = await fetchWithAuth('/vehiculos'); const container = $('flotaContainer'); container.replaceChildren();
    $('vehiculosActivos').textContent = flota.filter(v => v.estado !== 'MANTENIMIENTO').length;
    if (!flota.length) { container.textContent = 'Todavía no hay vehículos registrados.'; return; }
    flota.forEach(v => {
        const card = document.createElement('article'); card.className = 'envio-card';
        card.append(texto('h3', v.placa), texto('p', `${v.capacidadKg} kg · ${v.estado} · ${v.nombreEmpresa}`));
        const actions = document.createElement('div'); actions.className = 'card-actions';
        actions.append(boton('Editar', () => {
            $('editVehiculoId').value = v.id; $('placa').value = v.placa; $('capacidadKg').value = v.capacidadKg;
            $('estadoVehiculo').value = v.estado; $('empresaId').value = v.empresaId; $('placa').focus();
        }), boton('Eliminar', async () => {
            if (await confirmarAccion(`¿Eliminar el vehículo ${v.placa}? Debe estar libre de envíos asociados.`) === null) return;
            await fetchWithAuth(`/vehiculos/${v.id}`, { method: 'DELETE' });
            mensaje('Vehículo eliminado.'); await Promise.all([cargarFlota(), cargarCatalogos()]);
        }, 'btn-danger')); card.append(actions); container.append(card);
    });
}
function manejarFormulario(id, action) {
    $(id).addEventListener('submit', async event => {
        event.preventDefault(); const button = event.submitter; button.disabled = true; mensaje('');
        try { await action(); } catch (ex) { mensaje(ex.message, true); }
        finally { button.disabled = false; }
    });
}
document.addEventListener('DOMContentLoaded', async () => {
    if (!$('enviosGrid')) return;
    if (!sessionStorage.getItem('jwt_token')) { cerrarSesion(); return; }
    const expiration = Number(obtenerSesion().exp) * 1000;
    if (!expiration || expiration <= Date.now()) { cerrarSesion(); return; }
    $('welcomeUser').textContent = `${obtenerSesion().sub} · ${obtenerRoles().map(r => r.replace('ROLE_', '')).join(', ')}`;
    $('btnLogout').addEventListener('click', cerrarSesion);
    $('createEnvioSection').hidden = !tieneRol('ADMIN', 'OPERADOR'); $('tabFlota').hidden = !tieneRol('ADMIN');
    $('tabEnvios').addEventListener('click', () => { $('panelEnvios').hidden = false; $('panelFlota').hidden = true; });
    $('tabFlota').addEventListener('click', async () => {
        $('panelEnvios').hidden = true; $('panelFlota').hidden = false;
        try { await cargarFlota(); } catch (ex) { mensaje(ex.message, true); }
    });
    $('estadoFiltro').addEventListener('change', renderEnvios); $('buscarEnvio').addEventListener('input', renderEnvios);
    $('refreshEnvios').addEventListener('click', cargarEnvios);
    $('fechaDesde').addEventListener('change', renderBitacora); $('fechaHasta').addEventListener('change', renderBitacora);
    $('auditPanel').hidden = !tieneRol('ADMIN');
    document.querySelectorAll('[data-estado]').forEach(button => button.addEventListener('click', () => {
        $('estadoFiltro').value = button.dataset.estado; renderEnvios();
        document.querySelectorAll('[data-estado]').forEach(b => b.setAttribute('aria-pressed', String(b === button)));
    }));
    $('cancelEdit').addEventListener('click', () => { $('vehiculoForm').reset(); $('editVehiculoId').value = ''; });
    manejarFormulario('envioForm', async () => {
        await fetchWithAuth('/envios', { method: 'POST', body: JSON.stringify({
            codigoRastreo: $('codigoRastreo').value, direccionDestino: $('direccionDestino').value,
            pesoKg: Number($('pesoKg').value), costo: Number($('costo').value),
            vehiculoId: Number($('vehiculoId').value), conductorId: Number($('conductorId').value)
        }) }); $('envioForm').reset(); mensaje('Envío registrado correctamente.'); await cargarEnvios();
    });
    manejarFormulario('vehiculoForm', async () => {
        const id = $('editVehiculoId').value;
        await fetchWithAuth(id ? `/vehiculos/${id}` : '/vehiculos', { method: id ? 'PUT' : 'POST', body: JSON.stringify({
            placa: $('placa').value, capacidadKg: Number($('capacidadKg').value),
            estado: $('estadoVehiculo').value, empresaId: Number($('empresaId').value)
        }) }); $('vehiculoForm').reset(); $('editVehiculoId').value = ''; mensaje('Vehículo guardado.');
        await Promise.all([cargarFlota(), cargarCatalogos()]);
    });
    await cargarEnvios();
    try {
        const vehiculos = await fetchWithAuth('/catalogos/vehiculos');
        $('vehiculosActivos').textContent = vehiculos.filter(v => v.estado !== 'MANTENIMIENTO').length;
    } catch (ex) { mensaje(ex.message, true); }
    if (tieneRol('ADMIN', 'OPERADOR')) {
        try { await cargarCatalogos(); } catch (ex) { mensaje(ex.message, true); }
    }
});
