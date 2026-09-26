package cr.ac.ucr.paraiso.ie.c5c089.lab5.data;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.Conductor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Integer> {
}