package cr.ac.ucr.paraiso.ie.c5c089.lab5.controller;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.business.AuthService;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean AuthService authService;
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void login_credencialesCorrectas_retornaHttp200ConToken() throws Exception {
        AuthResponseDTO respuesta = new AuthResponseDTO();
        respuesta.setToken("jwt-de-prueba");
        respuesta.setUsername("admin");
        respuesta.setRoles(List.of("ROLE_ADMIN"));
        respuesta.setExpirationTime(1_800_000_000_000L);
        when(authService.autenticarUsuario(any())).thenReturn(respuesta);

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"Test123!\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-de-prueba"))
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void login_credencialesIncorrectas_retornaHttp401() throws Exception {
        when(authService.autenticarUsuario(any()))
            .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_camposVacios_retornaHttp400() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fields.username").exists())
            .andExpect(jsonPath("$.fields.password").exists());
    }
}
