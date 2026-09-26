package cr.ac.ucr.paraiso.ie.c5c089.lab5.dto;

import java.math.BigDecimal;

public class EnvioResponseDTO {
    private Integer id;
    private String codigoRastreo;
    private String direccionDestino;
    private BigDecimal pesoKg;
    private BigDecimal costo;
    private String estadoEnvio;
    private String placaVehiculo;
    private String nombreConductor;
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
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
    public String getEstadoEnvio() {
        return estadoEnvio;
    }
    public void setEstadoEnvio(String estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }
    public String getPlacaVehiculo() {
        return placaVehiculo;
    }
    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }
    public String getNombreConductor() {
        return nombreConductor;
    }
    public void setNombreConductor(String nombreConductor) {
        this.nombreConductor = nombreConductor;
    }
    

}