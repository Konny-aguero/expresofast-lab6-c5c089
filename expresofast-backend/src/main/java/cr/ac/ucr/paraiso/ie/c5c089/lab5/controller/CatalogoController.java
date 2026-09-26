package cr.ac.ucr.paraiso.ie.c5c089.lab5.controller;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.business.VehiculoService;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {
    private final VehiculoService service;
    public CatalogoController(VehiculoService service) { this.service=service; }
    @GetMapping("/vehiculos") public List<VehiculoResponseDTO> vehiculos() { return service.listar(); }
    @GetMapping("/conductores") public List<OpcionDTO> conductores() { return service.conductores(); }
    @GetMapping("/empresas") public List<OpcionDTO> empresas() { return service.empresas(); }
}
