package cr.ac.ucr.paraiso.ie.c5c089.lab5.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AsignacionVehiculoDTO(@NotNull @Positive Integer vehiculoId) {}
