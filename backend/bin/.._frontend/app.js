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
    $('enviosContainer').textContent = 'Cargando envíos…';
    try { envios = await fetchWithAuth('/envios/optimizados'); renderEnvios(); }
    catch (ex) { $('enviosContainer').textContent = 'No se pudieron cargar los envíos.'; mensaje(ex.message, true); }
}
function renderEnvios() {
    const container = $('enviosContainer'); container.replaceChildren();
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
        if (tieneRol('ADMIN', 'OPERADOR')) actions.append(boton('Ver bitácora', () => verBitacora(e.id)));
        if (tieneRol('ADMIN', 'CONDUCTOR')) {
            if (e.estadoEnvio === 'PENDIENTE') actions.append(boton('Marcar en tránsito', () => cambiarEstado(e.id, 'EN_TRANSITO'), 'btn-warning'));
            if (e.estadoEnvio === 'EN_TRANSITO') actions.append(boton('Marcar entregado', () => cambiarEstado(e.id, 'ENTREGADO'), 'btn-success'));
            if (['PENDIENTE', 'EN_TRANSITO'].includes(e.estadoEnvio)) actions.append(boton('Cancelar envío', () => cambiarEstado(e.id, 'CANCELADO'), 'btn-danger'));
        }
        card.append(actions); container.append(card);
    });
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
    await fetchWithAuth(`/envios/${id}/estado`, { method: 'PATCH', body: JSON.stringify({ nuevoEstado, observaciones }) });
    mensaje('Estado actualizado y cambio registrado en la bitácora.'); await cargarEnvios();
}
async function verBitacora(id) {
    bitacoras = await fetchWithAuth(`/envios/${id}/bitacora`);
    $('fechaDesde').value = ''; $('fechaHasta').value = ''; renderBitacora();
    $('bitacoraModal').showModal();
}
function renderBitacora() {
    const content = $('bitacoraContent'); content.replaceChildren();
    const desde = $('fechaDesde').value, hasta = $('fechaHasta').value;
    if (desde && hasta && desde > hasta) { content.textContent = 'La fecha inicial no puede ser posterior a la final.'; return; }
    const rows = bitacoras.filter(b => (!desde || b.fechaCambio.slice(0, 10) >= desde) && (!hasta || b.fechaCambio.slice(0, 10) <= hasta));
    if (!rows.length) { content.textContent = 'No hay registros de auditoría en este rango.'; return; }
    const table = document.createElement('table'); table.className = 'table-audit';
    const head = document.createElement('thead'), tr = document.createElement('tr');
    ['Fecha', 'Anterior', 'Nuevo', 'Usuario', 'Observaciones'].forEach(t => tr.append(texto('th', t)));
    head.append(tr); table.append(head); const body = document.createElement('tbody');
    rows.forEach(b => {
        const row = document.createElement('tr');
        [new Date(b.fechaCambio).toLocaleString('es-CR'), b.estadoAnterior, b.estadoNuevo, b.usuario, b.observaciones || '—']
            .forEach(value => row.append(texto('td', value)));
        body.append(row);
    }); table.append(body); content.append(table);
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
    if (!localStorage.getItem('jwt_token')) { cerrarSesion(); return; }
    const expiration = Number(localStorage.getItem('expirationTime'));
    if (expiration && expiration <= Date.now()) { cerrarSesion(); return; }
    $('welcomeUser').textContent = `${localStorage.getItem('username')} · ${obtenerRoles().map(r => r.replace('ROLE_', '')).join(', ')}`;
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
    $('closeModal').addEventListener('click', () => $('bitacoraModal').close());
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
    if (tieneRol('ADMIN', 'OPERADOR')) {
        try { await cargarCatalogos(); } catch (ex) { mensaje(ex.message, true); }
    }
});
