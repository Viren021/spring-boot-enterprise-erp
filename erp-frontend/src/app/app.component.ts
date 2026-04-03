import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { KeycloakService } from 'keycloak-angular'; // 🌟 MUST IMPORT KEYCLOAK

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'erp-frontend';

  // 🌟 INJECT KEYCLOAK INTO THE CONSTRUCTOR
  constructor(private keycloakService: KeycloakService) {}

  // 🌟 THE MISSING LOGOUT METHOD
  logout() {
    this.keycloakService.logout('http://localhost:4200');
  }
}
