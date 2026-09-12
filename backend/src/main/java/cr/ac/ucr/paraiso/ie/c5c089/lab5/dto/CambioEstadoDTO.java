package cr.ac.ucr.paraiso.ie.c5c089.lab5.dto;

import jakarta.validation.constraints.NotBlank;

public class CambioEstadoDTO {
    
    @NotBlank(message = "El nuevo estado es obligatorio")
    @jakarta.validation.constraints.Pattern(regexp="PENDIENTE|EN_TRANSITO|ENTREGADO|CANCELADO", message="Estado no válido")
    private String nuevoEstado;
    
    @jakarta.validation.constraints.Size(max=250)
    private String observaciones;

    public String getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(String nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    
}