package cr.ac.ucr.paraiso.ie.c5c089.lab5.controller;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.exception.ResourceNotFoundException;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnvioController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnvioControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean EnvioService service;
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listar_exitoso_retornaHttp200YJson() throws Exception {
        when(service.obtenerEnviosOptimizados()).thenReturn(List.of(respuesta()));

        mvc.perform(get("/api/envios/optimizados"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].codigoRastreo").value("EXP-1234"))
            .andExpect(jsonPath("$[0].estadoEnvio").value("PENDIENTE"));
    }

    @Test
    void crear_datosValidos_retornaHttp201() throws Exception {
        when(service.registrarEnvio(any())).thenReturn(respuesta());

        mvc.perform(post("/api/envios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "codigoRastreo": "EXP-1234",
                      "direccionDestino": "San José",
                      "pesoKg": 25.5,
                      "costo": 3500,
                      "vehiculoId": 10,
                      "conductorId": 20
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"));
    }

    @Test
    void crear_payloadInvalido_retornaHttp400ConErrores() throws Exception {
        mvc.perform(post("/api/envios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.fields.codigoRastreo").exists())
            .andExpect(jsonPath("$.fields.pesoKg").exists());
    }

    @Test
    void cambiarEstado_exitoso_retornaHttp200() throws Exception {
        EnvioResponseDTO respuesta = respuesta();
        respuesta.setEstadoEnvio("EN_TRANSITO");
        when(service.cambiarEstadoEnvio(eq(1), any())).thenReturn(respuesta);

        mvc.perform(patch("/api/envios/1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nuevoEstado\":\"EN_TRANSITO\",\"observaciones\":\"En ruta\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estadoEnvio").value("EN_TRANSITO"));
    }

    @Test
    void obtenerBitacora_envioInexistente_retornaHttp404() throws Exception {
        when(service.obtenerBitacoraPorEnvio(99))
            .thenThrow(new ResourceNotFoundException("Envío no encontrado"));

        mvc.perform(get("/api/envios/99/bitacora"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Envío no encontrado"));
    }

    private EnvioResponseDTO respuesta() {
        EnvioResponseDTO dto = new EnvioResponseDTO();
        dto.setId(1);
        dto.setCodigoRastreo("EXP-1234");
        dto.setDireccionDestino("San José");
        dto.setPesoKg(new BigDecimal("25.50"));
        dto.setCosto(new BigDecimal("3500.00"));
        dto.setEstadoEnvio("PENDIENTE");
        dto.setPlacaVehiculo("ABC-123");
        dto.setNombreConductor("Ana Mora");
        return dto;
    }
}
