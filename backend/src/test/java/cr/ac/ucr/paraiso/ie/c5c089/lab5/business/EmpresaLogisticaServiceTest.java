package cr.ac.ucr.paraiso.ie.c5c089.lab5.business;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.EmpresaLogisticaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EmpresaLogisticaServiceTest {

    @Mock EmpresaLogisticaRepository repository;
    @InjectMocks EmpresaLogisticaService service;

    @Test
    void crearServicio_repositorioInyectado_noRealizaConsultas() {
        assertNotNull(service);
        verifyNoInteractions(repository);
    }
}
