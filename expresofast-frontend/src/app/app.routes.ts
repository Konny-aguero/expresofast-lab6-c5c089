import { Routes } from '@angular/router';
import { EnvioListComponent } from './components/envio-list/envio-list.component';
import { EnvioAvanzadoFormComponent } from './components/envio-form/envio-form.component';
import { EnvioTrackingComponent } from './components/envio-tracking/envio-tracking.component';

export const routes: Routes = [
  { path: 'envios', component: EnvioListComponent },
  { path: 'nuevo-envio', component: EnvioAvanzadoFormComponent },
  { path: 'rastreo', component: EnvioTrackingComponent },
  { path: '', redirectTo: 'envios', pathMatch: 'full' },
  { path: '**', redirectTo: 'envios' }
];
