package cr.ac.ucr.paraiso.ie.c5c089.lab5.business;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.*;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.*;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.*;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.exception.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class EnvioService {
    private static final Map<String, Set<String>> TRANSICIONES = Map.of(
        "PENDIENTE", Set.of("EN_TRANSITO", "CANCELADO"),
        "EN_TRANSITO", Set.of("ENTREGADO", "CANCELADO"),
        "ENTREGADO", Set.of(), "CANCELADO", Set.of());
    private final EnvioRepository envios;
    private final VehiculoRepository vehiculos;
    private final ConductorRepository conductores;
    private final BitacoraEnvioRepository bitacoras;
    private final UsuarioRepository usuarios;
    public EnvioService(EnvioRepository envios, VehiculoRepository vehiculos, ConductorRepository conductores,
                        BitacoraEnvioRepository bitacoras, UsuarioRepository usuarios) {
        this.envios=envios; this.vehiculos=vehiculos; this.conductores=conductores;
        this.bitacoras=bitacoras; this.usuarios=usuarios;
    }
    @Transactional(readOnly=true)
    public List<EnvioResponseDTO> obtenerEnviosOptimizados() {
        return envios.findAllOptimizados().stream().map(this::respuesta).toList();
    }
    public EnvioResponseDTO registrarEnvio(EnvioRequestDTO dto) {
        Vehiculo vehiculo = vehiculos.findById(dto.getVehiculoId())
            .orElseThrow(() -> new ResourceNotFoundException("El vehículo especificado no existe"));
        Conductor conductor = conductores.findById(dto.getConductorId())
            .orElseThrow(() -> new ResourceNotFoundException("El conductor especificado no existe"));
        if (dto.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0)
            throw new IllegalArgumentException("El peso supera la capacidad del vehículo ("+vehiculo.getCapacidadKg()+" kg)");
        if ("MANTENIMIENTO".equals(vehiculo.getEstado()))
            throw new IllegalArgumentException("No se pueden asignar envíos a un vehículo en mantenimiento");
        Envio envio = new Envio();
        envio.setCodigoRastreo(dto.getCodigoRastreo()); envio.setDireccionDestino(dto.getDireccionDestino().trim());
        envio.setPesoKg(dto.getPesoKg()); envio.setCosto(dto.getCosto()); envio.setEstadoEnvio("PENDIENTE");
        envio.setVehiculo(vehiculo); envio.setConductor(conductor);
        return respuesta(envios.saveAndFlush(envio));
    }
    public EnvioResponseDTO cambiarEstadoEnvio(Integer id, CambioEstadoDTO dto) {
        // Serializa cambios simultáneos sin agregar columnas a la base de los laboratorios.
        Envio envio=envios.findByIdForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado"));
        String anterior=envio.getEstadoEnvio();
        if (!TRANSICIONES.getOrDefault(anterior, Set.of()).contains(dto.getNuevoEstado()))
            throw new InvalidStateTransitionException("Transición de estado no permitida para el envío "+envio.getCodigoRastreo());
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario=usuarios.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        envio.setEstadoEnvio(dto.getNuevoEstado());
        BitacoraEnvio bitacora=new BitacoraEnvio();
        bitacora.setEnvio(envio); bitacora.setUsuario(usuario); bitacora.setEstadoAnterior(anterior);
        bitacora.setEstadoNuevo(dto.getNuevoEstado()); bitacora.setFechaCambio(LocalDateTime.now());
        bitacora.setObservaciones(dto.getObservaciones()); bitacoras.saveAndFlush(bitacora);
        return respuesta(envio);
    }
    @Transactional(readOnly=true)
    public List<BitacoraResponseDTO> obtenerBitacoraPorEnvio(Integer id) {
        if (!envios.existsById(id)) throw new ResourceNotFoundException("Envío no encontrado");
        return bitacoras.findByEnvioId(id).stream().map(b -> {
            BitacoraResponseDTO dto=new BitacoraResponseDTO();
            dto.setId(b.getId()); dto.setEstadoAnterior(b.getEstadoAnterior()); dto.setEstadoNuevo(b.getEstadoNuevo());
            dto.setFechaCambio(b.getFechaCambio()); dto.setUsuario(b.getUsuario().getUsername());
            dto.setObservaciones(b.getObservaciones()); return dto;
        }).toList();
    }
    private EnvioResponseDTO respuesta(Envio e) {
        EnvioResponseDTO dto=new EnvioResponseDTO();
        dto.setId(e.getId()); dto.setCodigoRastreo(e.getCodigoRastreo()); dto.setDireccionDestino(e.getDireccionDestino());
        dto.setPesoKg(e.getPesoKg()); dto.setCosto(e.getCosto()); dto.setEstadoEnvio(e.getEstadoEnvio());
        dto.setPlacaVehiculo(e.getVehiculo().getPlaca());
        dto.setNombreConductor(e.getConductor().getNombre()+" "+e.getConductor().getApellidos());
        return dto;
    }
}
