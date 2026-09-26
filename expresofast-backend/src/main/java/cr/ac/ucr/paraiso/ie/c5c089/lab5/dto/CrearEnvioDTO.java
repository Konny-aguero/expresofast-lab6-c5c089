package cr.ac.ucr.paraiso.ie.c5c089.lab5.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CrearEnvioDTO(
    @NotBlank @Size(max = 100) String destinatario,
    @NotBlank @Size(max = 200) String direccionDestino,
    @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal montoFlete
) {}
