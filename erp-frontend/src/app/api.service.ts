import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { lastValueFrom, BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  // ✅ SINGLE API GATEWAY ENTRY POINT (no more hardcoded ports!)
  private readonly API_BASE_URL = 'http://localhost:8090/api/v1';

  // Loading state for UI
  private loading$ = new BehaviorSubject<boolean>(false);
  loading = this.loading$.asObservable();

  constructor(private http: HttpClient, private keycloak: KeycloakService) { }

  // =========================================================
  // SECURITY & TENANT IDENTITY HELPERS
  // =========================================================

  hasRole(roleName: string): boolean {
    const userRoles = this.keycloak.getUserRoles();
    return userRoles.includes(roleName);
  }

  // 🌟 Dynamically fetch the Tenant ID from the logged-in user!
  private async getTenantId(): Promise<string> {
    try {
      const tokenParsed = this.keycloak.getKeycloakInstance().tokenParsed;

      if (tokenParsed && tokenParsed['tenant_id']) {
        return tokenParsed['tenant_id'];
      }

      const profile = await this.keycloak.loadUserProfile();
      if (profile.attributes && profile.attributes['tenant_id']) {
        const tenantAttribute: any = profile.attributes['tenant_id'];
        return Array.isArray(tenantAttribute) ? tenantAttribute[0] : tenantAttribute;
      }

      throw new Error("Authenticated user has no tenant_id claim.");

    } catch (error) {
      throw new Error("Unable to determine tenant identity from Keycloak.");
    }
  }

  // 🌟 Centralized Header Builder
  private async getStandardHeaders(): Promise<HttpHeaders> {
    const token = await this.keycloak.getToken();
    const tenantId = await this.getTenantId();

    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'X-Tenant-ID': tenantId,
      'Content-Type': 'application/json'
    });
  }

  private startLoading() {
    this.loading$.next(true);
  }

  private stopLoading() {
    this.loading$.next(false);
  }

  // =========================================================
  // 1. EXECUTIVE / AI → Gateway routes to port 8085
  // =========================================================
  async getCeoDashboard() {
    const headers = await this.getStandardHeaders();
    const request = this.http.get(`${this.API_BASE_URL}/analytics/dashboard`, { headers });
    return await lastValueFrom(request);
  }

  async predictDemand() {
    const headers = await this.getStandardHeaders();
    const request = this.http.post(`${this.API_BASE_URL}/ai/forecast/demand/1`, {}, {
      headers,
      responseType: 'text'
    });
    return await lastValueFrom(request);
  }

  // =========================================================
  // 2. HR DEPARTMENT → Gateway routes to port 8082
  // =========================================================
  async getEmployees() {
    const headers = await this.getStandardHeaders();
    const request = this.http.get(`${this.API_BASE_URL}/hr/employees`, { headers });
    return await lastValueFrom(request);
  }

  async addEmployee(employeeData: any) {
    const headers = await this.getStandardHeaders();
    const request = this.http.post(`${this.API_BASE_URL}/hr/employees`, employeeData, { headers });
    return await lastValueFrom(request);
  }

  async updateEmployeeLifecycle(id: number, status: string, terminationDate?: string) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.patch(
      `${this.API_BASE_URL}/hr/employees/${id}/lifecycle`,
      { status, terminationDate },
      { headers }));
  }

  async getLeaveRequests(employeeId?: number) {
    const headers = await this.getStandardHeaders();
    const query = employeeId == null ? '' : `?employeeId=${employeeId}`;
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/hr/leaves${query}`, { headers }));
  }

  async submitLeaveRequest(requestData: any) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/hr/leaves`, requestData, { headers }));
  }

  async approveLeaveRequest(id: number) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/hr/leaves/${id}/approve`, {}, { headers }));
  }

  async rejectLeaveRequest(id: number, reason?: string) {
    const headers = await this.getStandardHeaders();
    const query = reason ? `?reason=${encodeURIComponent(reason)}` : '';
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/hr/leaves/${id}/reject${query}`, {}, { headers }));
  }

  async recordAttendance(record: any) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/hr/attendance`, record, { headers }));
  }

  async getExpenseClaims(employeeId?: number) {
    const headers = await this.getStandardHeaders();
    const query = employeeId == null ? '' : `?employeeId=${employeeId}`;
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/hr/expenses${query}`, { headers }));
  }

  async submitExpenseClaim(claim: any) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/hr/expenses`, claim, { headers }));
  }

  // =========================================================
  // 3. FINANCE DEPARTMENT → Gateway routes to port 8084
  // =========================================================
  async getLedger() {
    const headers = await this.getStandardHeaders();
    const request = this.http.get(`${this.API_BASE_URL}/finance/ledger/recent`, { headers });
    return await lastValueFrom(request);
  }

  async addTransaction(transactionData: any) {
    const headers = await this.getStandardHeaders();
    const request = this.http.post(`${this.API_BASE_URL}/finance/ledger/add`, transactionData, { headers });
    return await lastValueFrom(request);
  }

  async getAccounts() {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/finance/accounts`, { headers }));
  }

  async getFiscalPeriods() {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/finance/periods`, { headers }));
  }

  async getJournals() {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/finance/journals`, { headers }));
  }

  async postJournal(id: string) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/finance/journals/${id}/post`, {}, { headers }));
  }

  // =========================================================
  // 4. INVENTORY DEPARTMENT → Gateway routes to port 8083
  // =========================================================
  async triggerOrderSaga(productId: number, quantity: number) {
    const headers = await this.getStandardHeaders();
    const request = this.http.post(`${this.API_BASE_URL}/inventory/stock/trigger-saga`, { productId, quantity }, {
      headers,
      responseType: 'text'
    });
    return await lastValueFrom(request);
  }

  async getInventoryStats(): Promise<any> {
    const headers = await this.getStandardHeaders();
    const request = this.http.get(`${this.API_BASE_URL}/inventory/stats`, { headers });
    return await lastValueFrom(request);
  }

  async getWarehouses() {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/inventory/warehouses`, { headers }));
  }

  async getStockMovements(productId: number) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.get(
      `${this.API_BASE_URL}/inventory/stock/${productId}/movements`, { headers }));
  }

  async reserveStock(reservation: any) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(
      `${this.API_BASE_URL}/inventory/stock/reservations`, reservation, { headers }));
  }

  // =========================================================
  // 5. COMPLIANCE DEPARTMENT → Gateway routes to port 8086
  // =========================================================
  async getAuditLogs() {
    const headers = await this.getStandardHeaders();
    const request = this.http.get(`${this.API_BASE_URL}/compliance/audit-logs`, { headers });
    return await lastValueFrom(request);
  }

  // =========================================================
  // 6. PROCUREMENT → Gateway routes to port 8087 (NEW!)
  // =========================================================

  // --- Purchase Requests ---
  async createPurchaseRequest(prData: any) {
    const headers = await this.getStandardHeaders();
    const request = this.http.post(`${this.API_BASE_URL}/procurement/purchase-requests`, prData, { headers });
    return await lastValueFrom(request);
  }

  async getPurchaseRequests(): Promise<any[]> {
    const headers = await this.getStandardHeaders();
    const request = this.http.get<any[]>(`${this.API_BASE_URL}/procurement/purchase-requests`, { headers });
    return await lastValueFrom(request);
  }

  async approvePurchaseRequest(id: number, comments?: string) {
    const headers = await this.getStandardHeaders();
    const params = comments ? `?comments=${encodeURIComponent(comments)}` : '';
    const request = this.http.put(`${this.API_BASE_URL}/procurement/purchase-requests/${id}/approve${params}`, {}, { headers });
    return await lastValueFrom(request);
  }

  async submitPurchaseRequest(id: number) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.put(
      `${this.API_BASE_URL}/procurement/purchase-requests/${id}/submit`, {}, { headers }));
  }

  async rejectPurchaseRequest(id: number, comments?: string) {
    const headers = await this.getStandardHeaders();
    const params = comments ? `?comments=${encodeURIComponent(comments)}` : '';
    return await lastValueFrom(this.http.put(
      `${this.API_BASE_URL}/procurement/purchase-requests/${id}/reject${params}`, {}, { headers }));
  }

  // --- Purchase Orders ---
  async createPurchaseOrder(poData: any) {
    const headers = await this.getStandardHeaders();
    const request = this.http.post(`${this.API_BASE_URL}/procurement/purchase-orders`, poData, { headers });
    return await lastValueFrom(request);
  }

  async getPurchaseOrders(): Promise<any[]> {
    const headers = await this.getStandardHeaders();
    const request = this.http.get<any[]>(`${this.API_BASE_URL}/procurement/purchase-orders`, { headers });
    return await lastValueFrom(request);
  }

  async approvePurchaseOrder(id: number) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.put(
      `${this.API_BASE_URL}/procurement/purchase-orders/${id}/approve`, {}, { headers }));
  }

  // --- Receipts ---
  async receiveGoods(poId: number, receiptData: any) {
    const headers = await this.getStandardHeaders();
    const request = this.http.post(`${this.API_BASE_URL}/procurement/receipts/${poId}`, receiptData, { headers });
    return await lastValueFrom(request);
  }

  // --- Invoices ---
  async createInvoice(invoiceData: any) {
    const headers = await this.getStandardHeaders();
    const request = this.http.post(`${this.API_BASE_URL}/procurement/invoices`, invoiceData, { headers });
    return await lastValueFrom(request);
  }

  async performThreeWayMatch(invoiceId: number) {
    const headers = await this.getStandardHeaders();
    const request = this.http.post(`${this.API_BASE_URL}/procurement/invoices/${invoiceId}/three-way-match`, {}, { headers });
    return await lastValueFrom(request);
  }

  async getProcurementAuditHistory(documentType: string, documentId: number) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.get(
      `${this.API_BASE_URL}/procurement/${documentType}/${documentId}/audit-history`, { headers }));
  }

  // --- Vendors ---
  async getVendors() {
    const headers = await this.getStandardHeaders();
    const request = this.http.get(`${this.API_BASE_URL}/procurement/vendors`, { headers });
    return await lastValueFrom(request);
  }

  async createVendor(vendorData: any) {
    const headers = await this.getStandardHeaders();
    const request = this.http.post(`${this.API_BASE_URL}/procurement/vendors`, vendorData, { headers });
    return await lastValueFrom(request);
  }

  async updateVendor(id: number, vendorData: any) {
    const headers = await this.getStandardHeaders();
    const request = this.http.put(`${this.API_BASE_URL}/procurement/vendors/${id}`, vendorData, { headers });
    return await lastValueFrom(request);
  }

  // =========================================================
  // 7. SALES / ORDER-TO-CASH → Gateway routes to port 8088
  // =========================================================
  async getCustomers() {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/sales/customers`, { headers }));
  }

  async createCustomer(customer: any) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/sales/customers`, customer, { headers }));
  }

  async getSalesOrders() {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/sales/orders`, { headers }));
  }

  async createSalesOrder(order: any) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/sales/orders`, order, { headers }));
  }

  async getQuotations() {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/sales/quotations`, { headers }));
  }

  async createQuotation(quotation: any) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/sales/quotations`, quotation, { headers }));
  }

  async getSalesInvoices() {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/sales/invoices`, { headers }));
  }

  async createSalesInvoice(invoice: any) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/sales/invoices`, invoice, { headers }));
  }

  async recordSalesPayment(payment: any) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/sales/payments`, payment, { headers }));
  }

  async createSalesReturn(returnRequest: any) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/sales/returns`, returnRequest, { headers }));
  }

  async getMasterData(resource: string) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/master-data/${resource}`, { headers }));
  }

  async createMasterData(resource: string, value: any) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/master-data/${resource}`, value, { headers }));
  }

  async nextDocumentNumber(id: string) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/master-data/document-numbering/${id}/next`, {}, { headers }));
  }

  async updateSalesFulfillment(id: number, status: string) {
    const headers = await this.getStandardHeaders();
    return await lastValueFrom(this.http.patch(
      `${this.API_BASE_URL}/sales/orders/${id}/fulfillment-status`, { status }, { headers }));
  }

  async createAccount(account: any) { const headers = await this.getStandardHeaders(); return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/finance/accounts`, account, { headers })); }
  async createFiscalPeriod(period: any) { const headers = await this.getStandardHeaders(); return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/finance/periods`, period, { headers })); }
  async createJournal(journal: any) { const headers = await this.getStandardHeaders(); return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/finance/journals`, journal, { headers })); }

  async getPayrollPeriods() { const headers = await this.getStandardHeaders(); return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/hr/payroll/periods`, { headers })); }
  async createPayrollPeriod(period: any) { const headers = await this.getStandardHeaders(); return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/hr/payroll/periods`, period, { headers })); }
  async getPayrollRuns(periodId?: number) {
    const headers = await this.getStandardHeaders();
    const query = periodId == null ? '' : `?periodId=${periodId}`;
    return await lastValueFrom(this.http.get(`${this.API_BASE_URL}/hr/payroll/runs${query}`, { headers }));
  }
  async createPayrollRun(run: any) { const headers = await this.getStandardHeaders(); return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/hr/payroll/runs`, run, { headers })); }
  async processPayrollRun(id: number) { const headers = await this.getStandardHeaders(); return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/hr/payroll/runs/${id}/process`, {}, { headers })); }
  async approveExpenseClaim(id: number) { const headers = await this.getStandardHeaders(); return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/hr/expenses/${id}/approve`, {}, { headers })); }
  async rejectExpenseClaim(id: number, reason?: string) { const headers = await this.getStandardHeaders(); const query = reason ? `?reason=${encodeURIComponent(reason)}` : ''; return await lastValueFrom(this.http.post(`${this.API_BASE_URL}/hr/expenses/${id}/reject${query}`, {}, { headers })); }

  async getApprovalLeaves() { return await this.getLeaveRequests(); }
  async getApprovalExpenses() { return await this.getExpenseClaims(); }

}
