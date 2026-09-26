import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, tap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { CrearEnvioPayload, Envio, EstadoEnvio } from '../models/envio.model';

@Injectable({ providedIn: 'root' })
export class EnvioService {
  private http = inject(HttpClient);
  private url = environment.API_URL + 'envios';
  sesion = signal(!!sessionStorage.getItem('jwt_token'));

  iniciarSesion(username: string, password: string) {
    return this.http.post<{ token: string }>(new URL('../auth/login', environment.API_URL).href,
      { username, password }).pipe(tap(data => {
        sessionStorage.setItem('jwt_token', data.token);
        this.sesion.set(true);
      }), catchError(this.manejarError));
  }
  cerrarSesion() {
    sessionStorage.removeItem('jwt_token');
    this.sesion.set(false);
  }
  tieneRol(rol: string): boolean {
    try {
      const token = sessionStorage.getItem('jwt_token')!.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(token)).roles.includes('ROLE_' + rol);
    } catch { return false; }
  }
  estadosPermitidos(envio: Envio): EstadoEnvio[] {
    const siguientes: Record<EstadoEnvio, EstadoEnvio[]> = {
      PENDIENTE: ['EN_TRANSITO', 'CANCELADO'], EN_TRANSITO: ['ENTREGADO', 'CANCELADO'],
      ENTREGADO: [], CANCELADO: []
    };
    return siguientes[envio.estado].filter(estado => this.tieneRol('ADMIN')
      || (this.tieneRol('OPERADOR') && estado === 'EN_TRANSITO')
      || (this.tieneRol('CONDUCTOR') && estado === 'ENTREGADO'));
  }
  obtenerEnvios() {
    return this.http.get<Envio[]>(this.url).pipe(catchError(this.manejarError));
  }
  obtenerPorRastreo(codigo: string) {
    return this.http.get<Envio>(`${this.url}/rastreo/${encodeURIComponent(codigo.trim())}`)
      .pipe(catchError(this.manejarError));
  }
  crearEnvio(payload: CrearEnvioPayload) {
    return this.http.post<Envio>(this.url, payload).pipe(catchError(this.manejarError));
  }
  actualizarEstado(id: number, nuevoEstado: EstadoEnvio) {
    return this.http.patch<Envio>(`${this.url}/${id}/estado`, { nuevoEstado })
      .pipe(catchError(this.manejarError));
  }
  private manejarError = (error: HttpErrorResponse) => {
    if (error.status === 401) this.cerrarSesion();
    const mensaje = error.status === 0 ? 'No se pudo conectar con el backend.'
      : error.error?.detail || error.error?.error || 'No se pudo completar la operación.';
    return throwError(() => new Error(mensaje));
  };
}
