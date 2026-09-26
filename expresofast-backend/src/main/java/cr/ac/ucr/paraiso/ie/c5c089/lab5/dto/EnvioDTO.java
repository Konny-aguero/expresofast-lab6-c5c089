package cr.ac.ucr.paraiso.ie.c5c089.lab5.dto;

import cr.ac.ucr.paraiso.ie.c5c089.lab5.domain.Envio;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnvioDTO(Integer id, String codigoRastreo, String destinatario,
                       String direccionDestino, BigDecimal montoFlete, String estado,
                       LocalDateTime fechaCreacion) {
    public static EnvioDTO from(Envio envio) {
        return new EnvioDTO(envio.getId(), envio.getCodigoRastreo(), envio.getDestinatario(),
            envio.getDireccionDestino(), envio.getCosto(), envio.getEstadoEnvio(), envio.getFechaCreacion());
    }
}
