import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { EnvioService } from './services/envio.service';

@Component({
  selector: 'app-root', standalone: true,
  imports: [FormsModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.component.html', styleUrl: './app.component.css'
})
export class AppComponent {
  api = inject(EnvioService);
  username = '';
  password = '';
  error = '';
  cargando = false;
  ingresar() {
    this.cargando = true;
    this.error = '';
    this.api.iniciarSesion(this.username, this.password).subscribe({
      next: () => { this.password = ''; this.cargando = false; },
      error: error => { this.error = error.message; this.cargando = false; }
    });
  }
}
