package cr.ac.ucr.paraiso.ie.c5c089.lab5.controller;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {
    private final EnvioService service;
    public EnvioController(EnvioService service) { this.service = service; }
    @GetMapping("/optimizados")
    public List<EnvioResponseDTO> listar() { return service.obtenerEnviosOptimizados(); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnvioResponseDTO crear(@Valid @RequestBody EnvioRequestDTO dto) { return service.registrarEnvio(dto); }
    @PatchMapping("/{id}/estado")
    public EnvioResponseDTO cambiar(@PathVariable Integer id, @Valid @RequestBody CambioEstadoDTO dto) {
        return service.cambiarEstadoEnvio(id, dto);
    }
    @GetMapping("/{id}/bitacora")
    public List<BitacoraResponseDTO> bitacora(@PathVariable Integer id) { return service.obtenerBitacoraPorEnvio(id); }
}
