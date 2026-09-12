package cr.ac.ucr.paraiso.ie.c5c089.lab5.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record VehiculoRequestDTO(
    @NotBlank @Size(max=15) String placa,
    @NotNull @Positive @Digits(integer=8,fraction=2) BigDecimal capacidadKg,
    @NotBlank @Pattern(regexp="DISPONIBLE|EN_RUTA|MANTENIMIENTO") String estado,
    @NotNull @Positive Integer empresaId) {}
