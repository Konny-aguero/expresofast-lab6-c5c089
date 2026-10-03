package cr.ac.ucr.paraiso.ie.c5c089.lab5.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "PAQUETES")
public class Paquete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(name = "peso_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal pesoKg;

    public Long getId() { return id; }
    public Envio getEnvio() { return envio; }
    public void setEnvio(Envio envio) { this.envio = envio; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }
}
