package cr.ac.ucr.paraiso.ie.c5c089.lab5.controller;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.business.VehiculoService;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {
    private final VehiculoService service;
    public VehiculoController(VehiculoService service) { this.service=service; }
    @GetMapping public List<VehiculoResponseDTO> listar() { return service.listar(); }
    @GetMapping("/{id}") public VehiculoResponseDTO buscar(@PathVariable Integer id) { return service.buscar(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public VehiculoResponseDTO crear(@Valid @RequestBody VehiculoRequestDTO dto) { return service.guardar(null,dto); }
    @PutMapping("/{id}")
    public VehiculoResponseDTO actualizar(@PathVariable Integer id,@Valid @RequestBody VehiculoRequestDTO dto) { return service.guardar(id,dto); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) { service.eliminar(id); }
}
