const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const source = fs.readFileSync(path.join(__dirname, '../frontend/api.js'), 'utf8');
function setup(fetch, location = {protocol:'http:', port:'8080', origin:'http://localhost:8080'}) {
    const storage = new Map([['jwt_token','test-token'],['username','admin'],['roles','["ROLE_ADMIN"]'],['unrelated','preserved']]);
    const context = vm.createContext({fetch, Headers, location, window:{}, localStorage:{
        getItem:key => storage.get(key) ?? null, setItem:(k,v) => storage.set(k,v), removeItem:key => storage.delete(key)
    }});
    vm.runInContext(source,context); return {context,storage,location};
}
const response = (status,body) => ({status,ok:status>=200&&status<300,text:async()=>body});
test('usa el origen del backend y agrega JWT sin Content-Type a GET',async()=>{
    let call; const {context}=setup(async(url,options)=>{call={url,options}; return response(200,'[]');});
    await context.fetchWithAuth('/envios/optimizados');
    assert.equal(call.url,'http://localhost:8080/api/envios/optimizados');
    assert.equal(call.options.headers.get('Authorization'),'Bearer test-token');
    assert.equal(call.options.headers.has('Content-Type'),false);
});
test('el login POST manda JSON sin Authorization',async()=>{
    let call; const {context}=setup(async(url,options)=>{call=options;return response(200,'{"token":"ok"}');});
    await context.apiFetch('/auth/login',{method:'POST',body:'{}'},false);
    assert.equal(call.headers.get('Content-Type'),'application/json'); assert.equal(call.headers.has('Authorization'),false);
});
test('Live Server usa la API de 8080',async()=>{
    let url;const {context}=setup(async u=>{url=u;return response(200,'[]');},{protocol:'http:',port:'5500',origin:'http://localhost:5500'});
    await context.fetchWithAuth('/envios/optimizados');assert.equal(url,'http://localhost:8080/api/envios/optimizados');
});
test('401 cierra solo la sesión de ExpresoFast',async()=>{
    const {context,storage,location}=setup(async()=>response(401,'{"error":"Sesión vencida"}'));
    await assert.rejects(context.fetchWithAuth('/envios/optimizados'),/Sesión vencida/);
    assert.equal(storage.has('jwt_token'),false);assert.equal(storage.get('unrelated'),'preserved');assert.equal(location.href,'login.html');
});
test('403 informa permisos sin cerrar una sesión válida',async()=>{
    const {context,storage,location}=setup(async()=>response(403,'{"error":"No tiene permisos"}'));
    await assert.rejects(context.fetchWithAuth('/vehiculos'),/No tiene permisos/);
    assert.equal(storage.get('jwt_token'),'test-token');assert.equal(location.href,undefined);
});
test('204 acepta eliminación sin cuerpo JSON',async()=>{
    const {context}=setup(async()=>response(204,''));assert.equal(await context.fetchWithAuth('/vehiculos/2',{method:'DELETE'}),null);
});
test('errores de validación incluyen detalles de campos',async()=>{
    const {context}=setup(async()=>response(400,'{"error":"Revise los campos","fields":{"pesoKg":"debe ser positivo"}}'));
    await assert.rejects(context.fetchWithAuth('/envios'),/pesoKg: debe ser positivo/);
});
test('fallo de red informa conexión, sin acusar credenciales incorrectas',async()=>{
    const {context}=setup(async()=>{throw new TypeError('Failed to fetch');});
    await assert.rejects(context.apiFetch('/auth/login',{},false),/No se pudo conectar con el servidor/);
});
