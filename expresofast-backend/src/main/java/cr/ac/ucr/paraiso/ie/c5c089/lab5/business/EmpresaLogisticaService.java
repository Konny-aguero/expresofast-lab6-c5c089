package cr.ac.ucr.paraiso.ie.c5c089.lab5.business;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.EmpresaLogisticaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmpresaLogisticaService {

    private final EmpresaLogisticaRepository repository;

    public EmpresaLogisticaService(EmpresaLogisticaRepository repository) {
        this.repository = repository;
    }
}