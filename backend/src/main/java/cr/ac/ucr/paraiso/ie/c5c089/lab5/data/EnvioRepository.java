package cr.ac.ucr.paraiso.ie.c5c089.lab5.data;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Envio e WHERE e.id = :id")
    java.util.Optional<Envio> findByIdForUpdate(@Param("id") Integer id);
    boolean existsByVehiculoId(Integer vehiculoId);


    @Query("SELECT e FROM Envio e " +
           "JOIN FETCH e.vehiculo v " +
           "JOIN FETCH v.empresa " +
           "JOIN FETCH e.conductor")
    List<Envio> findAllOptimizados();

    @org.springframework.data.jpa.repository.query.Procedure(name = "Envio.porEstado")
    List<Envio> obtenerPorEstado(@Param("pEstado") String estado);

    @Query("""
        SELECT e FROM Envio e
        WHERE (:estado = '' OR e.estadoEnvio = :estado)
          AND (:conductorId IS NULL OR e.conductor.id = :conductorId)
          AND (LOWER(e.codigoRastreo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
            OR LOWER(e.destinatario) LIKE LOWER(CONCAT('%', :busqueda, '%'))
            OR LOWER(e.direccionDestino) LIKE LOWER(CONCAT('%', :busqueda, '%')))
        """)
    org.springframework.data.domain.Page<Envio> buscarPaginado(
        @Param("busqueda") String busqueda, @Param("estado") String estado,
        @Param("conductorId") Integer conductorId, org.springframework.data.domain.Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Envio e SET e.estadoEnvio = :estado WHERE e.vehiculo.id = :vehiculoId")
    void actualizarEstadoMasivoPorVehiculo(@Param("vehiculoId") Integer vehiculoId, @Param("estado") String estado);
}