package cr.ac.ucr.paraiso.ie.c5c089.lab5.controller;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.business.AuthService;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.AuthResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@jakarta.validation.Valid @RequestBody AuthRequestDTO authRequestDTO) {
        AuthResponseDTO response = authService.autenticarUsuario(authRequestDTO);
        return ResponseEntity.ok(response);
    }
}