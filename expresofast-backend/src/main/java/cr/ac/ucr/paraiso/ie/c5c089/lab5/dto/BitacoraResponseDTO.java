package cr.ac.ucr.paraiso.ie.c5c089.lab5.dto;


import java.time.LocalDateTime;

public class BitacoraResponseDTO {
    private Integer id;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaCambio;
    private String usuario;
    private String observaciones;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getEstadoAnterior() {
        return estadoAnterior;
    }
    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }
    public String getEstadoNuevo() {
        return estadoNuevo;
    }
    public void setEstadoNuevo(String estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }
    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }
    public void setFechaCambio(LocalDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
    }
    public String getUsuario() {
        return usuario;
    }
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    public String getObservaciones() {
        return observaciones;
    }
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    
}