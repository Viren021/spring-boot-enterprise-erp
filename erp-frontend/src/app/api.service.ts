import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { lastValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient, private keycloak: KeycloakService) { }

  // =========================================================
  // SECURITY & TENANT IDENTITY HELPERS
  // =========================================================

  hasRole(roleName: string): boolean {
    const userRoles = this.keycloak.getUserRoles();
    return userRoles.includes(roleName);
  }

// 🌟 NEW: Dynamically fetch the Tenant ID from the logged-in user!
  private async getTenantId(): Promise<string> {
    try {
      // 1. Peek inside the secure Keycloak token
      const tokenParsed = this.keycloak.getKeycloakInstance().tokenParsed;

      if (tokenParsed && tokenParsed['tenant_id']) {
        return tokenParsed['tenant_id'];
      }

      // 2. Fallback: Check the user profile attributes
      const profile = await this.keycloak.loadUserProfile();
      if (profile.attributes && profile.attributes['tenant_id']) {
        // 🌟 THE FIX: Tell TypeScript to treat this attribute as 'any'
        const tenantAttribute: any = profile.attributes['tenant_id'];

        // Keycloak sometimes returns arrays, sometimes strings. This handles both!
        return Array.isArray(tenantAttribute) ? tenantAttribute[0] : tenantAttribute;
      }

      // 3. Safe Default
      console.warn("No 'tenant_id' found in Keycloak profile. Defaulting to 'tata_motors'.");
      return 'tata_motors';

    } catch (error) {
      return 'tata_motors';
    }
  }

  // 🌟 NEW: Centralized Header Builder (No more copy-pasting!)
  private async getStandardHeaders(): Promise<HttpHeaders> {
    const token = await this.keycloak.getToken();
    const tenantId = await this.getTenantId(); // Gets the dynamic tenant

    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'X-Tenant-ID': tenantId, // Injects it securely!
      'Content-Type': 'application/json'
    });
  }

  // =========================================================
  // 1. EXECUTIVE / AI (Port 8085)
  // =========================================================
  async getCeoDashboard() {
    const headers = await this.getStandardHeaders(); // Clean and simple!
    const request = this.http.get('http://localhost:8085/api/v1/analytics/dashboard', { headers });
    return await lastValueFrom(request);
  }

  async predictDemand() {
    const headers = await this.getStandardHeaders();
    const request = this.http.post('http://localhost:8085/api/v1/ai/forecast/demand/1', {}, {
      headers,
      responseType: 'text'
    });
    return await lastValueFrom(request);
  }

  // =========================================================
  // 2. HR DEPARTMENT (Port 8082)
  // =========================================================
  async getEmployees() {
    const headers = await this.getStandardHeaders();
    const request = this.http.get('http://localhost:8082/api/v1/hr/employees', { headers });
    return await lastValueFrom(request);
  }

  async addEmployee(employeeData: any) {
    const headers = await this.getStandardHeaders();
    const request = this.http.post('http://localhost:8082/api/v1/hr/employees', employeeData, { headers });
    return await lastValueFrom(request);
  }

  // =========================================================
  // 3. FINANCE DEPARTMENT (Port 8084)
  // =========================================================
  async getLedger() {
    const headers = await this.getStandardHeaders();
    const request = this.http.get('http://localhost:8084/api/v1/finance/ledger/recent', { headers });
    return await lastValueFrom(request);
  }

  async addTransaction(transactionData: any) {
    const headers = await this.getStandardHeaders();
    const request = this.http.post('http://localhost:8084/api/v1/finance/ledger/add', transactionData, { headers });
    return await lastValueFrom(request);
  }

  // =========================================================
    // 4. INVENTORY DEPARTMENT (Port 8083)
    // =========================================================
    async triggerOrderSaga() {
      const headers = await this.getStandardHeaders();
      const request = this.http.post('http://localhost:8083/api/v1/inventory/stock/trigger-saga', {}, {
        headers,
        responseType: 'text'
      });
      return await lastValueFrom(request);
    }

    // 🌟 PULLING REAL-TIME DATA FROM SPRING BOOT
    async getInventoryStats(): Promise<any> {
      const headers = await this.getStandardHeaders();

      // This hits your actual Inventory Microservice database!
      const request = this.http.get('http://localhost:8083/api/v1/inventory/stats', { headers });
      return await lastValueFrom(request);
    }

  // =========================================================
  // 5. COMPLIANCE DEPARTMENT (Port 8086)
  // =========================================================
  async getAuditLogs() {
    const headers = await this.getStandardHeaders();
    const request = this.http.get('http://localhost:8086/api/v1/compliance/audit-logs', { headers });
    return await lastValueFrom(request);
  }

}
