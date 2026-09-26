const elemento = id => document.getElementById(id);
let pagina = 0;
let totalPaginas = 0;
let cargando = false;

function botones(primera = true, ultima = true) {
    elemento('primera').disabled = primera;
    elemento('anterior').disabled = primera;
    elemento('siguiente').disabled = ultima;
    elemento('ultima').disabled = ultima;
}

async function cargarPagina(numero = 0) {
    if (cargando) return;
    cargando = true;
    botones();
    elemento('error').textContent = '';
    const controles = elemento('filtros').querySelectorAll('input, select, button');
    controles.forEach(control => { control.disabled = true; });
    const procedimiento = elemento('procedimiento').value;
    try {
        const parametros = new URLSearchParams({ page: numero, size: elemento('size').value,
            busqueda: elemento('busqueda').value.trim(), estado: elemento('estado').value,
            sortBy: 'fechaCreacion', direction: 'desc' });
        const data = await fetchWithAuth(procedimiento
            ? `/v1/envios/procedimiento/${encodeURIComponent(procedimiento)}`
            : `/v1/envios/paginados?${parametros}`);
        const filas = procedimiento ? data : data.content;
        elemento('envios').replaceChildren();
        filas.forEach(envio => {
            const fila = document.createElement('tr');
            [envio.codigoRastreo, envio.destinatario || 'Sin registrar', envio.direccionDestino,
                new Intl.NumberFormat('es-CR', { style: 'currency', currency: 'CRC' }).format(envio.montoFlete),
                envio.estado].forEach(valor => {
                const celda = document.createElement('td');
                celda.textContent = valor;
                fila.append(celda);
            });
            elemento('envios').append(fila);
        });
        if (procedimiento) {
            elemento('indicador').textContent = `Procedimiento: ${procedimiento} (Total: ${filas.length} envíos)`;
        } else {
            pagina = data.number;
            totalPaginas = data.totalPages;
            elemento('indicador').textContent = `Página ${totalPaginas ? pagina + 1 : 0} de ${totalPaginas} (Total: ${data.totalElements} envíos)`;
            botones(data.first, data.last || !totalPaginas);
        }
    } catch (error) {
        elemento('envios').replaceChildren();
        elemento('indicador').textContent = '';
        elemento('error').textContent = error.message;
    } finally {
        cargando = false;
        controles.forEach(control => { control.disabled = false; });
        ['busqueda', 'estado', 'size'].forEach(id => { elemento(id).disabled = Boolean(procedimiento); });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (!(Number(obtenerSesion().exp) * 1000 > Date.now())) { cerrarSesion(); return; }
    elemento('salir').addEventListener('click', cerrarSesion);
    elemento('filtros').addEventListener('submit', event => { event.preventDefault(); cargarPagina(); });
    ['size', 'estado', 'procedimiento'].forEach(id => elemento(id).addEventListener('change', () => cargarPagina()));
    elemento('primera').addEventListener('click', () => cargarPagina(0));
    elemento('anterior').addEventListener('click', () => cargarPagina(pagina - 1));
    elemento('siguiente').addEventListener('click', () => cargarPagina(pagina + 1));
    elemento('ultima').addEventListener('click', () => cargarPagina(totalPaginas - 1));
    cargarPagina();
});
