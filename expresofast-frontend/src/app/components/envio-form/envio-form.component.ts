import { Component, inject } from '@angular/core';
import { AsyncValidatorFn, FormArray, FormControl, FormGroup, NonNullableFormBuilder,
  ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { catchError, map, of, switchMap, timer } from 'rxjs';
import { EnvioService } from '../../services/envio.service';

export const fechasValidas: ValidatorFn = group => {
  const despacho = group.get('fechaDespacho')?.value;
  const entrega = group.get('fechaEntregaEstimada')?.value;
  return despacho && entrega && Date.parse(entrega) <= Date.parse(despacho)
    ? { fechasInvalidas: true } : null;
};

export function trackingDisponible(api: EnvioService): AsyncValidatorFn {
  return control => timer(300).pipe(
    switchMap(() => api.checkTracking(control.value)),
    map(existe => existe ? { trackingTomado: true } : null),
    catchError(() => of({ trackingNoVerificado: true }))
  );
}

type PaqueteForm = FormGroup<{
  descripcion: FormControl<string>;
  pesoKg: FormControl<number>;
}>;

@Component({
  selector: 'app-envio-avanzado-form', standalone: true, imports: [ReactiveFormsModule],
  templateUrl: './envio-form.component.html', styleUrl: './envio-form.component.css'
})
export class EnvioAvanzadoFormComponent {
  api = inject(EnvioService);
  private fb = inject(NonNullableFormBuilder);
  form = this.fb.group({
    numeroTracking: this.fb.control('', {
      validators: [Validators.required, Validators.maxLength(30), Validators.pattern(/\S/)],
      asyncValidators: [trackingDisponible(this.api)]
    }),
    destinatario: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/\S/)]],
    direccionDestino: ['', [Validators.required, Validators.maxLength(200), Validators.pattern(/\S/)]],
    montoFlete: [0, [Validators.required, Validators.min(0.01), Validators.max(99999999.99)]],
    fechaDespacho: ['', Validators.required],
    fechaEntregaEstimada: ['', Validators.required],
    paquetes: new FormArray<PaqueteForm>([this.nuevoPaquete()], Validators.minLength(1))
  }, { validators: fechasValidas });
  error = '';
  codigo = '';
  guardando = false;

  get paquetes() { return this.form.controls.paquetes; }

  private nuevoPaquete(): PaqueteForm {
    return this.fb.group({
      descripcion: ['', [Validators.required, Validators.maxLength(255), Validators.pattern(/\S/)]],
      pesoKg: [0, [Validators.required, Validators.min(0.01), Validators.max(999.99),
        Validators.pattern(/^\d+(\.\d{1,2})?$/)]]
    });
  }
  agregarPaquete() { this.paquetes.push(this.nuevoPaquete()); }
  eliminarPaquete(indice: number) {
    if (this.paquetes.length > 1) this.paquetes.removeAt(indice);
  }
  guardar() {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.form.pending || this.guardando) return;
    this.guardando = true;
    this.error = this.codigo = '';
    this.api.crearEnvio(this.form.getRawValue()).subscribe({
      next: envio => {
        this.codigo = envio.codigoRastreo;
        this.paquetes.clear();
        this.paquetes.push(this.nuevoPaquete());
        this.form.reset();
        this.guardando = false;
      },
      error: error => { this.error = error.message; this.guardando = false; }
    });
  }
}
