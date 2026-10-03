package cr.ac.ucr.paraiso.ie.c5c089.lab5.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CrearEnvioDTO(
    @NotBlank @Size(max = 30) String numeroTracking,
    @NotNull java.time.LocalDate fechaDespacho,
    @NotNull java.time.LocalDate fechaEntregaEstimada,
    @NotEmpty java.util.List<@NotNull @jakarta.validation.Valid PaqueteDTO> paquetes,
    @NotBlank @Size(max = 100) String destinatario,
    @NotBlank @Size(max = 200) String direccionDestino,
    @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal montoFlete
) {}
