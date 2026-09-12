package cr.ac.ucr.paraiso.ie.c5c089.lab5;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class Lab5ApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired jakarta.persistence.EntityManager entityManager;
    static final String PASSWORD="Test123!";
    static final String HASH=new BCryptPasswordEncoder().encode(PASSWORD);
    @BeforeEach void seed() {
        jdbc.update("INSERT INTO Rol(rol_id,nombre_rol) VALUES(1,'ROLE_ADMIN'),(2,'ROLE_OPERADOR'),(3,'ROLE_CONDUCTOR')");
        for(int i=1;i<=3;i++) {
            String name=new String[]{"admin","operador","conductor1"}[i-1];
            jdbc.update("INSERT INTO Usuario(usuario_id,username,password_hash,nombre_completo,email,activo) VALUES(?,?,?,?,?,1)",i,name,HASH,name,name+"@test.local");
            jdbc.update("INSERT INTO UsuarioRol(usuario_id,rol_id) VALUES(?,?)",i,i);
        }
        jdbc.update("INSERT INTO EmpresaLogistica(empresa_id,nombre,cedula_juridica,telefono,fecha_registro) VALUES(1,'Test','TEST-01','2222',GETDATE())");
        jdbc.update("INSERT INTO Vehiculo(vehiculo_id,placa,capacidad_kg,estado,empresa_id) VALUES(10000,'TEST-01',100,'DISPONIBLE',1)");
        jdbc.update("INSERT INTO Conductor(conductor_id,nombre,apellidos,licencia,telefono) VALUES(1,'Carlos','Test','LIC-01','8888')");
    }
    String token(String username) throws Exception {
        String body=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\""+username+"\",\"password\":\""+PASSWORD+"\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.roles",everyItem(startsWith("ROLE_"))))
            .andExpect(jsonPath("$.expirationTime",greaterThan(System.currentTimeMillis())))
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body,"$.token");
    }
    String envio(String codigo, int vehiculo, int conductor, String peso) {
        if (vehiculo == 1) vehiculo = 10000;
        return "{\"codigoRastreo\":\""+codigo+"\",\"direccionDestino\":\"Paraíso\",\"pesoKg\":"+peso+",\"costo\":3500,\"vehiculoId\":"+vehiculo+",\"conductorId\":"+conductor+"}";
    }
    int crear(String auth) throws Exception {
        String result=mvc.perform(post("/api/envios").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON)
            .content(envio("EXP-1234",1,1,"10"))).andExpect(status().isCreated())
            .andExpect(jsonPath("$.placaVehiculo").value("TEST-01"))
            .andExpect(jsonPath("$.nombreConductor").value("Carlos Test"))
            .andExpect(jsonPath("$.vehiculo").doesNotExist()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(result,"$.id");
    }
    @Test void frontendEsPublico() throws Exception {
        mvc.perform(get("/login.html")).andExpect(status().isOk()).andExpect(content().string(containsString("loginForm")));
        mvc.perform(get("/api.js")).andExpect(status().isOk());
    }
    @Test void corsPermitePreflightSinToken() throws Exception {
        for(String method:new String[]{"POST","PATCH","DELETE"})
            mvc.perform(options("/api/envios/1/estado").header("Origin","http://127.0.0.1:5500")
                .header("Access-Control-Request-Method",method).header("Access-Control-Request-Headers","authorization,content-type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://127.0.0.1:5500"));
    }
    @Test void rechazaCredencialesIncorrectasYCamposVacios() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.username").exists());
    }
    @Test void rechazaTokenAusenteOInvalido() throws Exception {
        mvc.perform(get("/api/envios/optimizados")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/envios/optimizados").header("Authorization","Bearer invalido")).andExpect(status().isUnauthorized());
    }
    @Test void respetaUsuariosInactivosInclusoConTokenEmitido() throws Exception {
        String auth=token("admin"); jdbc.update("UPDATE Usuario SET activo=0 WHERE username='admin'"); entityManager.clear();
        mvc.perform(get("/api/envios/optimizados").header("Authorization","Bearer "+auth)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"admin\",\"password\":\"Test123!\"}"))
            .andExpect(status().isUnauthorized());
    }
    @Test void flujoCompletoConDtoYBitacora() throws Exception {
        String admin=token("admin"); int id=crear(admin);
        mvc.perform(get("/api/envios/optimizados").header("Authorization","Bearer "+admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].codigoRastreo").value("EXP-1234"));
        mvc.perform(patch("/api/envios/"+id+"/estado").header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nuevoEstado\":\"EN_TRANSITO\",\"observaciones\":\"Saliendo a ruta\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estadoEnvio").value("EN_TRANSITO"));
        mvc.perform(get("/api/envios/"+id+"/bitacora").header("Authorization","Bearer "+admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].usuario").value("admin"))
            .andExpect(jsonPath("$[0].estadoAnterior").value("PENDIENTE"))
            .andExpect(jsonPath("$[0].observaciones").value("Saliendo a ruta"))
            .andExpect(jsonPath("$[0].envio").doesNotExist())
            .andExpect(content().string(not(containsString("passwordHash"))));
        assertNotNull(jdbc.queryForObject("SELECT fecha_creacion FROM Envio WHERE envio_id=?",java.sql.Timestamp.class,id));
        assertNotNull(jdbc.queryForObject("SELECT fecha_modificacion FROM Envio WHERE envio_id=?",java.sql.Timestamp.class,id));
    }
    @Test void operadorPuedeCrearYVerBitacoraPeroNoCambiarEstadoNiGestionarFlota() throws Exception {
        String auth=token("operador"); int id=crear(auth);
        mvc.perform(get("/api/catalogos/vehiculos").header("Authorization","Bearer "+auth)).andExpect(status().isOk());
        mvc.perform(get("/api/envios/"+id+"/bitacora").header("Authorization","Bearer "+auth)).andExpect(status().isOk());
        mvc.perform(patch("/api/envios/"+id+"/estado").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nuevoEstado\":\"EN_TRANSITO\"}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/vehiculos").header("Authorization","Bearer "+auth)).andExpect(status().isForbidden());
    }
    @Test void conductorPuedeCambiarPeroNoCrearNiVerBitacora() throws Exception {
        int id=crear(token("admin")); String auth=token("conductor1");
        mvc.perform(get("/api/envios/optimizados").header("Authorization","Bearer "+auth)).andExpect(status().isOk());
        mvc.perform(patch("/api/envios/"+id+"/estado").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nuevoEstado\":\"EN_TRANSITO\"}")).andExpect(status().isOk());
        mvc.perform(post("/api/envios").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON)
            .content(envio("EXP-9999",1,1,"1"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/envios/"+id+"/bitacora").header("Authorization","Bearer "+auth)).andExpect(status().isForbidden());
    }
    @Test void validacionEvitaNulosEstadosYExcesoDeLongitud() throws Exception {
        String auth=token("admin");
        mvc.perform(post("/api/envios").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.vehiculoId").exists()).andExpect(jsonPath("$.fields.pesoKg").exists());
        mvc.perform(post("/api/envios").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON).content(envio("INVALID",1,1,"1")))
            .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/envios/1/estado").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nuevoEstado\":\"INVALIDO\"}")).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/envios/1/estado").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nuevoEstado\":\"EN_TRANSITO\",\"observaciones\":\""+"x".repeat(251)+"\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void validaCapacidadYReferencias() throws Exception {
        String auth=token("admin");
        mvc.perform(post("/api/envios").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON).content(envio("EXP-1111",1,1,"101")))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/envios").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON).content(envio("EXP-1111",999,1,"1")))
            .andExpect(status().isNotFound());
        mvc.perform(post("/api/envios").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON).content(envio("EXP-1111",1,999,"1")))
            .andExpect(status().isNotFound());
    }
    @Test void noReabreEstadosFinalesNiDuplicaBitacoras() throws Exception {
        String auth=token("admin"); int id=crear(auth);
        for(String estado:new String[]{"EN_TRANSITO","ENTREGADO"})
            mvc.perform(patch("/api/envios/"+id+"/estado").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nuevoEstado\":\""+estado+"\"}")).andExpect(status().isOk());
        for(String estado:new String[]{"PENDIENTE","EN_TRANSITO","CANCELADO","ENTREGADO"})
            mvc.perform(patch("/api/envios/"+id+"/estado").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nuevoEstado\":\""+estado+"\"}")).andExpect(status().isBadRequest());
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM BitacoraEnvio",Integer.class));
        assertEquals("ENTREGADO",jdbc.queryForObject("SELECT estado_envio FROM Envio WHERE envio_id=?",String.class,id));
    }
    @Test void devuelve404EnBitacoraInexistente() throws Exception {
        mvc.perform(get("/api/envios/999/bitacora").header("Authorization","Bearer "+token("admin"))).andExpect(status().isNotFound());
    }
    @Test void flotaCrudYProteccionDeEnviosAsociados() throws Exception {
        String auth=token("admin");
        String body=mvc.perform(post("/api/vehiculos").header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON)
            .content("{\"placa\":\"NEW-01\",\"capacidadKg\":200,\"estado\":\"DISPONIBLE\",\"empresaId\":1}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int id=JsonPath.read(body,"$.id");
        mvc.perform(put("/api/vehiculos/"+id).header("Authorization","Bearer "+auth).contentType(MediaType.APPLICATION_JSON)
            .content("{\"placa\":\"NEW-01\",\"capacidadKg\":250,\"estado\":\"MANTENIMIENTO\",\"empresaId\":1}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.capacidadKg").value(250));
        mvc.perform(delete("/api/vehiculos/"+id).header("Authorization","Bearer "+auth)).andExpect(status().isNoContent());
        crear(auth);
        mvc.perform(delete("/api/vehiculos/10000").header("Authorization","Bearer "+auth)).andExpect(status().isConflict());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM Envio",Integer.class));
    }
}
