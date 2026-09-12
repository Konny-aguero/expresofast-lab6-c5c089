package cr.ac.ucr.paraiso.ie.c5c089.lab5.data;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT v FROM Vehiculo v JOIN FETCH v.empresa ORDER BY v.placa")
    java.util.List<Vehiculo> findAllConEmpresa();
}