package cr.ac.ucr.paraiso.ie.c5c089.lab5.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PaqueteDTO(
    @NotBlank @Size(max = 255) String descripcion,
    @NotNull @DecimalMin("0.01") @Digits(integer = 3, fraction = 2) BigDecimal pesoKg
) {}
