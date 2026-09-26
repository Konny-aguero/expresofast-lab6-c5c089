package cr.ac.ucr.paraiso.ie.c5c089.lab5.business;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.*;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.*;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.*;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.exception.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
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
    @Transactional(readOnly = true)
    public List<EnvioDTO> obtenerEnvios(String estado) {
        validarEstado(estado);
        Integer conductorId = conductorVisible();
        if (Integer.valueOf(-1).equals(conductorId)) return List.of();
        var lista = estado.isEmpty()
            ? envios.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion", "id"))
            : envios.findByEstadoEnvioOrderByFechaCreacionDesc(estado);
        return lista.stream().filter(e -> esVisible(e, conductorId)).map(EnvioDTO::from).toList();
    }
    @Transactional(readOnly = true)
    public EnvioDTO obtenerPorRastreo(String codigo) {
        Envio envio = envios.findByCodigoRastreo(codigo.trim())
            .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado"));
        if (!esVisible(envio, conductorVisible())) throw new ResourceNotFoundException("Envío no encontrado");
        return EnvioDTO.from(envio);
    }
    public EnvioDTO crearEnvio(CrearEnvioDTO dto) {
        Envio envio = new Envio();
        envio.setCodigoRastreo("EXP-" + java.time.Year.now().getValue() + "-"
            + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 21).toUpperCase());
        envio.setDestinatario(dto.destinatario().trim());
        envio.setDireccionDestino(dto.direccionDestino().trim());
        envio.setCosto(dto.montoFlete());
        envio.setPesoKg(java.math.BigDecimal.ZERO);
        envio.setEstadoEnvio("PENDIENTE");
        return EnvioDTO.from(envios.saveAndFlush(envio));
    }
    public EnvioDTO actualizarEstado(Integer id, CambioEstadoDTO dto) {
        cambiarEstadoEnvio(id, dto);
        return EnvioDTO.from(envios.findById(id).orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado")));
    }
    private boolean esVisible(Envio envio, Integer conductorId) {
        return conductorId == null || (envio.getConductor() != null && conductorId.equals(envio.getConductor().getId()));
    }
    @Transactional(readOnly=true)
    public List<EnvioResponseDTO> obtenerEnviosOptimizados() {
        Integer conductorId = null;
        if (!tieneRol("ADMIN") && !tieneRol("OPERADOR")) {
            Usuario usuario = usuarioActual();
            if (usuario.getConductor() == null) return List.of();
            conductorId = usuario.getConductor().getId();
        }
        Integer asignado = conductorId;
        return envios.findAllOptimizados().stream()
            .filter(e -> esVisible(e, asignado))
            .map(this::respuesta).toList();
    }
    @Transactional(readOnly = true)
    public Page<EnvioDTO> listarPaginado(int page, int size, String sortBy, String dir,
                                        String busqueda, String estado) {
        var campos = Map.of("id", "id", "codigoRastreo", "codigoRastreo", "destinatario", "destinatario",
            "direccionDestino", "direccionDestino", "montoFlete", "costo", "estado", "estadoEnvio",
            "fechaCreacion", "fechaCreacion");
        if (page < 0 || size < 1 || size > 100 || !campos.containsKey(sortBy)
                || !("asc".equalsIgnoreCase(dir) || "desc".equalsIgnoreCase(dir)))
            throw new IllegalArgumentException("Paginación u ordenamiento inválido (size: 1 a 100)");
        validarEstado(estado);
        var sort = Sort.by(Sort.Direction.fromString(dir), campos.get(sortBy)).and(Sort.by("id"));
        var pageable = PageRequest.of(page, size, sort);
        Integer conductorId = conductorVisible();
        if (Integer.valueOf(-1).equals(conductorId)) return Page.empty(pageable);
        return envios.buscarPaginado(busqueda.trim(), estado, conductorId, pageable).map(EnvioDTO::from);
    }

    @Transactional(readOnly = true)
    public List<EnvioDTO> listarViaStoredProcedure(String estado) {
        validarEstado(estado);
        if (estado.isEmpty()) throw new IllegalArgumentException("Seleccione un estado");
        Integer conductorId = conductorVisible();
        if (Integer.valueOf(-1).equals(conductorId)) return List.of();
        return envios.obtenerPorEstado(estado).stream()
            .filter(e -> esVisible(e, conductorId))
            .map(EnvioDTO::from).toList();
    }

    private void validarEstado(String estado) {
        if (!estado.isEmpty() && !TRANSICIONES.containsKey(estado))
            throw new IllegalArgumentException("Estado inválido");
    }

    private Integer conductorVisible() {
        if (tieneRol("ADMIN") || tieneRol("OPERADOR")) return null;
        Usuario usuario = usuarioActual();
        return usuario.getConductor() == null ? -1 : usuario.getConductor().getId();
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
        Envio envio=envios.findByIdForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado"));
        String anterior=envio.getEstadoEnvio();
        if (!TRANSICIONES.getOrDefault(anterior, Set.of()).contains(dto.getNuevoEstado()))
            throw new InvalidStateTransitionException("Transición de estado no permitida para el envío "+envio.getCodigoRastreo());
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario=usuarios.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!tieneRol("ADMIN")) {
            if (tieneRol("OPERADOR")) {
                if (!"EN_TRANSITO".equals(dto.getNuevoEstado()))
                    throw new AccessDeniedException("El operador solo puede iniciar el tránsito");
            } else if (!tieneRol("CONDUCTOR") || usuario.getConductor() == null
                    || envio.getConductor() == null
                    || !usuario.getConductor().getId().equals(envio.getConductor().getId())
                    || !"ENTREGADO".equals(dto.getNuevoEstado())) {
                throw new AccessDeniedException("El conductor solo puede entregar sus envíos asignados");
            }
        }
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
    public EnvioResponseDTO asignarVehiculo(Integer id, Integer vehiculoId) {
        Envio envio = envios.findByIdForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado"));
        if (!"PENDIENTE".equals(envio.getEstadoEnvio()))
            throw new IllegalArgumentException("Solo se puede asignar un vehículo a un envío pendiente");
        Vehiculo vehiculo = vehiculos.findById(vehiculoId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
        if ("MANTENIMIENTO".equals(vehiculo.getEstado()) || envio.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0)
            throw new IllegalArgumentException("El vehículo no está disponible o no tiene capacidad suficiente");
        envio.setVehiculo(vehiculo);
        return respuesta(envio);
    }
    private boolean tieneRol(String rol) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }
    private Usuario usuarioActual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AccessDeniedException("Sesión requerida");
        return usuarios.findByUsername(auth.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
    private EnvioResponseDTO respuesta(Envio e) {
        EnvioResponseDTO dto=new EnvioResponseDTO();
        dto.setId(e.getId()); dto.setCodigoRastreo(e.getCodigoRastreo()); dto.setDireccionDestino(e.getDireccionDestino());
        dto.setPesoKg(e.getPesoKg()); dto.setCosto(e.getCosto()); dto.setEstadoEnvio(e.getEstadoEnvio());
        dto.setPlacaVehiculo(e.getVehiculo() == null ? "Sin asignar" : e.getVehiculo().getPlaca());
        dto.setNombreConductor(e.getConductor() == null ? "Sin asignar" : e.getConductor().getNombre()+" "+e.getConductor().getApellidos());
        return dto;
    }
}
