package cr.ac.ucr.paraiso.ie.c5c089.lab5.business;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.ConductorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConductorService {

    private final ConductorRepository repository;

    public ConductorService(ConductorRepository repository) {
        this.repository = repository;
    }
}