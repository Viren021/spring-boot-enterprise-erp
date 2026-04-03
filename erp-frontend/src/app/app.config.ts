import { APP_INITIALIZER, ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { KeycloakService } from 'keycloak-angular';
import { provideHttpClient } from '@angular/common/http';

// This function runs BEFORE the website is allowed to load
function initializeKeycloak(keycloak: KeycloakService) {
  return () =>
    keycloak.init({
      config: {
        url: 'http://localhost:8180', // Your Docker Keycloak
        realm: 'erp-realm',
        clientId: 'erp-frontend' // The client we just verified!
      },
      initOptions: {
        onLoad: 'login-required', // Forces the user to the login screen immediately
        checkLoginIframe: false   // Prevents modern browser cookie warnings
      }
    });
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(), // Add this line!
    KeycloakService,
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService]
    }
  ]
};
