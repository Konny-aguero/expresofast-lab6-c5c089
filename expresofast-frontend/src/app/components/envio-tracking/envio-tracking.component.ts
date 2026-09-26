import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EnvioService } from '../../services/envio.service';
import { Envio } from '../../models/envio.model';

@Component({
  selector: 'app-envio-tracking', standalone: true, imports: [CommonModule, FormsModule],
  templateUrl: './envio-tracking.component.html', styleUrl: './envio-tracking.component.css'
})
export class EnvioTrackingComponent {
  private api = inject(EnvioService);
  codigo = '';
  envio: Envio | null = null;
  error = '';
  buscando = false;
  buscar() {
    if (!this.codigo.trim()) return;
    this.envio = null;
    this.error = '';
    this.buscando = true;
    this.api.obtenerPorRastreo(this.codigo).subscribe({
      next: envio => { this.envio = envio; this.buscando = false; },
      error: error => { this.error = error.message; this.buscando = false; }
    });
  }
}
