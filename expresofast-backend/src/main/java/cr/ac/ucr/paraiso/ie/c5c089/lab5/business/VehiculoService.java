package cr.ac.ucr.paraiso.ie.c5c089.lab5.business;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.data.*;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.*;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.dto.*;
import cr.ac.ucr.paraiso.ie.c5c089.lab5.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;

@Service
@Transactional(readOnly=true)
public class VehiculoService {
    private final VehiculoRepository vehiculos;
    private final EmpresaLogisticaRepository empresas;
    private final ConductorRepository conductores;
    private final EnvioRepository envios;
    public VehiculoService(VehiculoRepository vehiculos, EmpresaLogisticaRepository empresas,
                           ConductorRepository conductores, EnvioRepository envios) {
        this.vehiculos=vehiculos; this.empresas=empresas; this.conductores=conductores; this.envios=envios;
    }
    public List<VehiculoResponseDTO> listar() { return vehiculos.findAllConEmpresa().stream().map(this::dto).toList(); }
    public VehiculoResponseDTO buscar(Integer id) { return dto(entidad(id)); }
    public List<OpcionDTO> empresas() { return empresas.findAll().stream().map(e -> new OpcionDTO(e.getId(),e.getNombre())).toList(); }
    public List<OpcionDTO> conductores() { return conductores.findAll().stream().map(c -> new OpcionDTO(c.getId(),c.getNombre()+" "+c.getApellidos())).toList(); }
    @Transactional
    public VehiculoResponseDTO guardar(Integer id, VehiculoRequestDTO request) {
        Vehiculo v=id==null ? new Vehiculo() : entidad(id);
        EmpresaLogistica empresa=empresas.findById(request.empresaId())
            .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
        v.setPlaca(request.placa().trim()); v.setCapacidadKg(request.capacidadKg());
        v.setEstado(request.estado()); v.setEmpresa(empresa);
        return dto(vehiculos.saveAndFlush(v));
    }
    @Transactional
    public void eliminar(Integer id) {
        Vehiculo v=entidad(id);
        if (envios.existsByVehiculoId(id))
            throw new DataIntegrityViolationException("Vehículo con envíos asociados");
        vehiculos.delete(v); vehiculos.flush();
    }
    private Vehiculo entidad(Integer id) { return vehiculos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado")); }
    private VehiculoResponseDTO dto(Vehiculo v) {
        return new VehiculoResponseDTO(v.getId(),v.getPlaca(),v.getCapacidadKg(),v.getEstado(),v.getEmpresa().getId(),v.getEmpresa().getNombre());
    }
}
