package cr.ac.ucr.paraiso.ie.c5c089.lab5.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class EnvioRequestDTO {

    @NotBlank(message = "El código de rastreo es obligatorio")
    @Pattern(regexp="^EXP-\\d{4}$", message = "Formato inválido. Ejemplo: EXP-1234")
    private String codigoRastreo;

    @NotBlank(message = "La dirección de destino es obligatoria")
    @Size(max=200)
    private String direccionDestino;

    @Positive(message = "El peso debe ser mayor a cero")
    @NotNull
    @Digits(integer=8, fraction=2)
    private BigDecimal pesoKg;

    @Positive(message = "El costo debe ser mayor a cero")
    @NotNull
    @Digits(integer=8, fraction=2)
    private BigDecimal costo;

    @NotNull
    @Positive
    private Integer vehiculoId;
    
    @NotNull
    @Positive
    private Integer conductorId;

    public String getCodigoRastreo() {
        return codigoRastreo;
    }

    public void setCodigoRastreo(String codigoRastreo) {
        this.codigoRastreo = codigoRastreo;
    }

    public String getDireccionDestino() {
        return direccionDestino;
    }

    public void setDireccionDestino(String direccionDestino) {
        this.direccionDestino = direccionDestino;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public Integer getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Integer vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public Integer getConductorId() {
        return conductorId;
    }

    public void setConductorId(Integer conductorId) {
        this.conductorId = conductorId;
    }

    
}