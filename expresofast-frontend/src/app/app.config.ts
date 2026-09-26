import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { environment } from '../environments/environment';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withFetch(), withInterceptors([(request, next) => {
      const token = sessionStorage.getItem('jwt_token');
      return next(token && request.url.startsWith(environment.API_URL)
        ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request);
    }]))
  ]
};
