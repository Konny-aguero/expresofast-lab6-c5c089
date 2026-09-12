package cr.ac.ucr.paraiso.ie.c5c089.lab5.dto;

import java.math.BigDecimal;
public record VehiculoResponseDTO(Integer id, String placa, BigDecimal capacidadKg, String estado,
    Integer empresaId, String nombreEmpresa) {}
