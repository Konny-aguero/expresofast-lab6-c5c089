package cr.ac.ucr.paraiso.ie.c5c089.lab5.data;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.BitacoraEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitacoraEnvioRepository extends JpaRepository<BitacoraEnvio, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT b FROM BitacoraEnvio b JOIN FETCH b.usuario WHERE b.envio.id = :envioId ORDER BY b.fechaCambio DESC, b.id DESC")
    List<BitacoraEnvio> findByEnvioId(@org.springframework.data.repository.query.Param("envioId") Integer envioId);
}