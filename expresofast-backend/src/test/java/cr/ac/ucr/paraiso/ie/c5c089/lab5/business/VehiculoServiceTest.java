package cr.ac.ucr.paraiso.ie.c5c089.lab5.business;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.VehiculoRequestDTO;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.VehiculoResponseDTO;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock VehiculoRepository vehiculos;
    @Mock EmpresaLogisticaRepository empresas;
    @Mock ConductorRepository conductores;
    @Mock EnvioRepository envios;
    @InjectMocks VehiculoService service;

    @Test
    void listar_vehiculosExistentes_retornaDtos() {
        when(vehiculos.findAllConEmpresa()).thenReturn(List.of(vehiculo()));

        List<VehiculoResponseDTO> resultado = service.listar();

        assertEquals(1, resultado.size());
        assertEquals("ABC-123", resultado.getFirst().placa());
        assertEquals("ExpresoFast", resultado.getFirst().nombreEmpresa());
    }

    @Test
    void guardar_datosValidos_persisteVehiculo() {
        EmpresaLogistica empresa = empresa();
        VehiculoRequestDTO request = new VehiculoRequestDTO(
            "  XYZ-789  ", new BigDecimal("750.00"), "DISPONIBLE", 5
        );
        when(empresas.findById(5)).thenReturn(Optional.of(empresa));
        when(vehiculos.saveAndFlush(any(Vehiculo.class))).thenAnswer(invocation -> {
            Vehiculo guardado = invocation.getArgument(0);
            guardado.setId(12);
            return guardado;
        });

        VehiculoResponseDTO resultado = service.guardar(null, request);

        assertEquals(12, resultado.id());
        assertEquals("XYZ-789", resultado.placa());
        assertEquals(new BigDecimal("750.00"), resultado.capacidadKg());
        assertEquals(5, resultado.empresaId());
    }

    @Test
    void guardar_empresaInexistente_lanzaExcepcion() {
        VehiculoRequestDTO request = new VehiculoRequestDTO(
            "XYZ-789", new BigDecimal("750.00"), "DISPONIBLE", 99
        );
        when(empresas.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> service.guardar(null, request));
        verify(vehiculos, never()).saveAndFlush(any());
    }

    @Test
    void eliminar_vehiculoConEnvios_lanzaExcepcion() {
        when(vehiculos.findById(10)).thenReturn(Optional.of(vehiculo()));
        when(envios.existsByVehiculoId(10)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class,
            () -> service.eliminar(10));
        verify(vehiculos, never()).delete(any());
    }

    @Test
    void eliminar_vehiculoSinEnvios_eliminaYConfirmaCambios() {
        Vehiculo vehiculo = vehiculo();
        when(vehiculos.findById(10)).thenReturn(Optional.of(vehiculo));
        when(envios.existsByVehiculoId(10)).thenReturn(false);

        service.eliminar(10);

        verify(vehiculos).delete(vehiculo);
        verify(vehiculos).flush();
    }

    private Vehiculo vehiculo() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10);
        vehiculo.setPlaca("ABC-123");
        vehiculo.setCapacidadKg(new BigDecimal("500.00"));
        vehiculo.setEstado("DISPONIBLE");
        vehiculo.setEmpresa(empresa());
        return vehiculo;
    }

    private EmpresaLogistica empresa() {
        EmpresaLogistica empresa = new EmpresaLogistica();
        empresa.setId(5);
        empresa.setNombre("ExpresoFast");
        return empresa;
    }
}
