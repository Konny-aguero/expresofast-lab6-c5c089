const API_URL = window.EXPRESOFAST_API_URL ||
    ((location.protocol === 'file:' || ['5500', '5501'].includes(location.port))
        ? 'http://localhost:8080/api' : `${location.origin}/api`);
const SESSION_KEYS = ['jwt_token', 'username', 'roles', 'expirationTime'];
function cerrarSesion() {
    SESSION_KEYS.forEach(key => sessionStorage.removeItem(key));
    location.href = 'index.html';
}
function obtenerSesion() {
    try {
        const payload = sessionStorage.getItem('jwt_token').split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(new TextDecoder().decode(Uint8Array.from(atob(payload), c => c.charCodeAt(0))));
    } catch { return {}; }
}
function obtenerRoles() {
    const roles = obtenerSesion().roles;
    return Array.isArray(roles) ? roles.filter(rol => rol.startsWith('ROLE_')) : [];
}
function tieneRol(...roles) { return roles.some(rol => obtenerRoles().includes(`ROLE_${rol}`)); }
async function apiFetch(path, options = {}, autenticado = true) {
    const headers = new Headers(options.headers);
    if (options.body) headers.set('Content-Type', 'application/json');
    if (autenticado) headers.set('Authorization', `Bearer ${sessionStorage.getItem('jwt_token') || ''}`);
    let response;
    try { response = await fetch(`${API_URL}${path}`, { ...options, headers }); }
    catch { throw new Error('No se pudo conectar con el servidor. Compruebe que el backend esté iniciado y la dirección de la API sea correcta.'); }
    if (autenticado && [401, 403].includes(response.status)) cerrarSesion();
    const text = await response.text();
    let data;
    try { data = text ? JSON.parse(text) : null; }
    catch { throw new Error('El servidor no devolvió JSON. Revise la URL de la API y que el backend esté disponible.'); }
    if (!response.ok) {
        const fields = data?.fields ? Object.entries(data.fields).map(([key, value]) => `${key}: ${value}`).join('; ') : '';
        throw new Error([data?.detail || data?.error || data?.title || `Error HTTP ${response.status}`, fields].filter(Boolean).join('. '));
    }
    return data;
}
function fetchWithAuth(path, options = {}) { return apiFetch(path, options); }
