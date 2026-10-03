export type EstadoEnvio = 'PENDIENTE' | 'EN_TRANSITO' | 'ENTREGADO' | 'CANCELADO';

export interface Paquete {
  descripcion: string;
  pesoKg: number;
}
export interface CrearEnvioPayload {
  numeroTracking: string;
  fechaDespacho: string;
  fechaEntregaEstimada: string;
  paquetes: Paquete[];
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
}
export interface Envio {
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  id: number;
  codigoRastreo: string;
  estado: EstadoEnvio;
  fechaCreacion: string;
}
