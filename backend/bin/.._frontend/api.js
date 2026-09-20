/* Misma URL para el frontend servido por Spring; compatible con Live Server. */
const API_URL = window.EXPRESOFAST_API_URL ||
    ((location.protocol === 'file:' || ['5500', '5501'].includes(location.port))
        ? 'http://localhost:8080/api' : `${location.origin}/api`);
const SESSION_KEYS = ['jwt_token', 'username', 'roles', 'expirationTime'];
function cerrarSesion() {
    SESSION_KEYS.forEach(key => localStorage.removeItem(key));
    location.href = 'login.html';
}
function obtenerRoles() {
    try { const roles = JSON.parse(localStorage.getItem('roles') || '[]'); return Array.isArray(roles) ? roles : []; }
    catch { return []; }
}
function tieneRol(...roles) { return roles.some(rol => obtenerRoles().includes(`ROLE_${rol}`)); }
async function apiFetch(path, options = {}, autenticado = true) {
    const headers = new Headers(options.headers);
    if (options.body) headers.set('Content-Type', 'application/json');
    if (autenticado) headers.set('Authorization', `Bearer ${localStorage.getItem('jwt_token') || ''}`);
    let response;
    try { response = await fetch(`${API_URL}${path}`, { ...options, headers }); }
    catch { throw new Error('No se pudo conectar con el servidor. Compruebe que el backend esté iniciado y la dirección de la API sea correcta.'); }
    const text = await response.text();
    let data;
    try { data = text ? JSON.parse(text) : null; }
    catch { throw new Error('El servidor no devolvió JSON. Revise la URL de la API y que el backend esté disponible.'); }
    if (!response.ok) {
        if (autenticado && response.status === 401) cerrarSesion();
        const fields = data?.fields ? Object.entries(data.fields).map(([key, value]) => `${key}: ${value}`).join('; ') : '';
        throw new Error([data?.error || `Error HTTP ${response.status}`, fields].filter(Boolean).join('. '));
    }
    return data;
}
function fetchWithAuth(path, options = {}) { return apiFetch(path, options); }
