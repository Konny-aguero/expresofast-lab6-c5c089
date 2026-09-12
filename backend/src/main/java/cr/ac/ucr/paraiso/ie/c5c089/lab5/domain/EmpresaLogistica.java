package cr.ac.ucr.paraiso.ie.c5c089.lab5.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "EmpresaLogistica")
public class EmpresaLogistica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empresa_id")
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "cedula_juridica", nullable = false, unique = true, length = 20)
    private String cedulaJuridica;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Vehiculo> vehiculos;

    public EmpresaLogistica() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCedulaJuridica() {
        return cedulaJuridica;
    }

    public void setCedulaJuridica(String cedulaJuridica) {
        this.cedulaJuridica = cedulaJuridica;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public List<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public void setVehiculos(List<Vehiculo> vehiculos) {
        this.vehiculos = vehiculos;
    }

    
}