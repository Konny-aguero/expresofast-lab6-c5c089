import { Component, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { EnvioService } from '../../services/envio.service';

@Component({
  selector: 'app-envio-form', standalone: true, imports: [FormsModule],
  templateUrl: './envio-form.component.html', styleUrl: './envio-form.component.css'
})
export class EnvioFormComponent {
  api = inject(EnvioService);
  destinatario = '';
  direccionDestino = '';
  montoFlete: number | null = null;
  error = '';
  codigo = '';
  guardando = false;
  guardar(form: NgForm) {
    if (form.invalid || !this.destinatario.trim() || !this.direccionDestino.trim() || this.montoFlete === null) return;
    this.guardando = true;
    this.error = this.codigo = '';
    this.api.crearEnvio({ destinatario: this.destinatario.trim(), direccionDestino: this.direccionDestino.trim(),
      montoFlete: this.montoFlete }).subscribe({
      next: envio => { this.codigo = envio.codigoRastreo; this.guardando = false; form.resetForm(); },
      error: error => { this.error = error.message; this.guardando = false; }
    });
  }
}
