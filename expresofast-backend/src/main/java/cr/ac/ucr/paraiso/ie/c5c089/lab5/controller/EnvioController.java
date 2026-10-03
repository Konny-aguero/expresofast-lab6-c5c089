package cr.ac.ucr.paraiso.ie.c5c089.lab5.controller;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class EnvioController {
    private final EnvioService service;
    public EnvioController(EnvioService service) { this.service = service; }
    @GetMapping({"/api/envios", "/api/envios/optimizados"})
    public List<EnvioResponseDTO> listar() { return service.obtenerEnviosOptimizados(); }
    @GetMapping("/api/v1/envios")
    public List<EnvioDTO> listarTodos(@RequestParam(defaultValue = "") String estado) {
        return service.obtenerEnvios(estado);
    }
    @GetMapping("/api/v1/envios/rastreo/{codigo}")
    public EnvioDTO rastrear(@PathVariable String codigo) { return service.obtenerPorRastreo(codigo); }
    @GetMapping("/api/envios/check-tracking/{trackingNumber}")
    public boolean checkTracking(@PathVariable String trackingNumber) {
        return service.existeTracking(trackingNumber);
    }
    @PostMapping("/api/v1/envios")
    @ResponseStatus(HttpStatus.CREATED)
    public EnvioDTO registrar(@Valid @RequestBody CrearEnvioDTO dto) { return service.crearEnvio(dto); }
    @PatchMapping("/api/v1/envios/{id}/estado")
    public EnvioDTO actualizarEstado(@PathVariable Integer id, @Valid @RequestBody CambioEstadoDTO dto) {
        return service.actualizarEstado(id, dto);
    }
    @GetMapping("/api/v1/envios/paginados")
    public org.springframework.data.domain.Page<EnvioDTO> listarPaginado(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "") String busqueda, @RequestParam(defaultValue = "") String estado) {
        return service.listarPaginado(page, size, sortBy, direction, busqueda, estado);
    }
    @GetMapping("/api/v1/envios/procedimiento/{estado}")
    public List<EnvioDTO> procedimiento(@PathVariable String estado) {
        return service.listarViaStoredProcedure(estado);
    }
    @PostMapping("/api/envios")
    @ResponseStatus(HttpStatus.CREATED)
    public EnvioResponseDTO crear(@Valid @RequestBody EnvioRequestDTO dto) { return service.registrarEnvio(dto); }
    @RequestMapping(value = "/api/envios/{id}/estado", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public EnvioResponseDTO cambiar(@PathVariable Integer id, @Valid @RequestBody CambioEstadoDTO dto) {
        return service.cambiarEstadoEnvio(id, dto);
    }
    @PutMapping("/api/envios/{id}/vehiculo")
    public EnvioResponseDTO asignar(@PathVariable Integer id, @Valid @RequestBody AsignacionVehiculoDTO dto) {
        return service.asignarVehiculo(id, dto.vehiculoId());
    }
    @GetMapping("/api/envios/{id}/bitacora")
    public List<BitacoraResponseDTO> bitacora(@PathVariable Integer id) { return service.obtenerBitacoraPorEnvio(id); }
}
