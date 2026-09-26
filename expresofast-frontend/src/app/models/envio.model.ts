export type EstadoEnvio = 'PENDIENTE' | 'EN_TRANSITO' | 'ENTREGADO' | 'CANCELADO';

export interface CrearEnvioPayload {
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
}
export interface Envio extends CrearEnvioPayload {
  id: number;
  codigoRastreo: string;
  estado: EstadoEnvio;
  fechaCreacion: string;
}
