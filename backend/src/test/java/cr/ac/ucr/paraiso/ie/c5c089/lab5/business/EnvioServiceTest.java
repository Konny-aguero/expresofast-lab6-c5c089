package cr.ac.ucr.paraiso.ie.c5c089.lab5.business;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.BitacoraEnvioRepository;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.BitacoraEnvio;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock EnvioRepository envios;
    @Mock VehiculoRepository vehiculos;
    @Mock ConductorRepository conductores;
    @Mock BitacoraEnvioRepository bitacoras;
    @Mock UsuarioRepository usuarios;
    @InjectMocks EnvioService service;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarEnvio_datosValidos_retornaEnvioPendiente() {
        Vehiculo vehiculo = vehiculo(new BigDecimal("100.00"));
        Conductor conductor = conductor();
        when(vehiculos.findById(10)).thenReturn(Optional.of(vehiculo));
        when(conductores.findById(20)).thenReturn(Optional.of(conductor));
        when(envios.saveAndFlush(any(Envio.class))).thenAnswer(invocation -> {
            Envio guardado = invocation.getArgument(0);
            guardado.setId(1);
            return guardado;
        });

        EnvioResponseDTO resultado = service.registrarEnvio(solicitud(new BigDecimal("25.50")));

        assertEquals(1, resultado.getId());
        assertEquals("EXP-1234", resultado.getCodigoRastreo());
        assertEquals("PENDIENTE", resultado.getEstadoEnvio());
        assertEquals("San José", resultado.getDireccionDestino());
        assertEquals("ABC-123", resultado.getPlacaVehiculo());
        assertEquals("Ana Mora", resultado.getNombreConductor());
    }

    @Test
    void registrarEnvio_vehiculoSinCapacidad_lanzaExcepcion() {
        when(vehiculos.findById(10)).thenReturn(Optional.of(vehiculo(new BigDecimal("10.00"))));
        when(conductores.findById(20)).thenReturn(Optional.of(conductor()));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> service.registrarEnvio(solicitud(new BigDecimal("10.01")))
        );

        assertEquals("El peso supera la capacidad del vehículo (10.00 kg)", error.getMessage());
        verify(envios, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @CsvSource({"ENTREGADO, EN_TRANSITO", "CANCELADO, PENDIENTE"})
    void cambiarEstado_transicionInvalida_lanzaExcepcion(String estadoActual, String estadoNuevo) {
        Envio envio = envio(estadoActual);
        CambioEstadoDTO cambio = new CambioEstadoDTO();
        cambio.setNuevoEstado(estadoNuevo);
        when(envios.findByIdForUpdate(1)).thenReturn(Optional.of(envio));

        assertThrows(InvalidStateTransitionException.class,
            () -> service.cambiarEstadoEnvio(1, cambio));

        verify(bitacoras, never()).saveAndFlush(any());
    }

    @Test
    void cambiarEstado_transicionValida_guardaBitacora() {
        Envio envio = envio("PENDIENTE");
        Usuario usuario = new Usuario();
        CambioEstadoDTO cambio = new CambioEstadoDTO();
        cambio.setNuevoEstado("EN_TRANSITO");
        cambio.setObservaciones("Salida de bodega");
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("operador", "clave")
        );
        when(envios.findByIdForUpdate(1)).thenReturn(Optional.of(envio));
        when(usuarios.findByUsername("operador")).thenReturn(Optional.of(usuario));

        EnvioResponseDTO resultado = service.cambiarEstadoEnvio(1, cambio);

        assertEquals("EN_TRANSITO", resultado.getEstadoEnvio());
        ArgumentCaptor<BitacoraEnvio> captor = ArgumentCaptor.forClass(BitacoraEnvio.class);
        verify(bitacoras).saveAndFlush(captor.capture());
        assertEquals("PENDIENTE", captor.getValue().getEstadoAnterior());
        assertEquals("EN_TRANSITO", captor.getValue().getEstadoNuevo());
        assertEquals("Salida de bodega", captor.getValue().getObservaciones());
    }

    @Test
    void obtenerBitacora_envioInexistente_lanzaExcepcion() {
        when(envios.existsById(99)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
            () -> service.obtenerBitacoraPorEnvio(99));
        verify(bitacoras, never()).findByEnvioId(any());
    }

    private EnvioRequestDTO solicitud(BigDecimal peso) {
        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setCodigoRastreo("EXP-1234");
        dto.setDireccionDestino("  San José  ");
        dto.setPesoKg(peso);
        dto.setCosto(new BigDecimal("3500.00"));
        dto.setVehiculoId(10);
        dto.setConductorId(20);
        return dto;
    }

    private Vehiculo vehiculo(BigDecimal capacidad) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10);
        vehiculo.setPlaca("ABC-123");
        vehiculo.setCapacidadKg(capacidad);
        vehiculo.setEstado("DISPONIBLE");
        return vehiculo;
    }

    private Conductor conductor() {
        Conductor conductor = new Conductor();
        conductor.setId(20);
        conductor.setNombre("Ana");
        conductor.setApellidos("Mora");
        return conductor;
    }

    private Envio envio(String estado) {
        Envio envio = new Envio();
        envio.setId(1);
        envio.setCodigoRastreo("EXP-1234");
        envio.setDireccionDestino("San José");
        envio.setPesoKg(new BigDecimal("25.50"));
        envio.setCosto(new BigDecimal("3500.00"));
        envio.setEstadoEnvio(estado);
        envio.setVehiculo(vehiculo(new BigDecimal("100.00")));
        envio.setConductor(conductor());
        return envio;
    }
}
