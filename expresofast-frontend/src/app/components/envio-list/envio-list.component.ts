import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EnvioService } from '../../services/envio.service';
import { Envio, EstadoEnvio } from '../../models/envio.model';

@Component({
  selector: 'app-envio-list', standalone: true, imports: [CommonModule, FormsModule],
  templateUrl: './envio-list.component.html', styleUrl: './envio-list.component.css'
})
export class EnvioListComponent implements OnInit {
  api = inject(EnvioService);
  envios: Envio[] = [];
  error = '';
  mensaje = '';
  cargando = true;
  actualizando: number | null = null;
  ngOnInit() {
    this.api.obtenerEnvios().subscribe({
      next: envios => { this.envios = envios; this.cargando = false; },
      error: error => { this.error = error.message; this.cargando = false; }
    });
  }
  cambiar(envio: Envio, estado: EstadoEnvio, selector: HTMLSelectElement) {
    selector.value = envio.estado;
    if (estado === envio.estado) return;
    this.actualizando = envio.id;
    this.error = this.mensaje = '';
    this.api.actualizarEstado(envio.id, estado).subscribe({
      next: actualizado => {
        this.envios = this.envios.map(e => e.id === actualizado.id ? actualizado : e);
        this.actualizando = null;
        this.mensaje = 'Estado actualizado.';
      },
      error: error => { this.error = error.message; this.actualizando = null; }
    });
  }
}
