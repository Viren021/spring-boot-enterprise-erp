import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { InventoryComponent } from './inventory/inventory.component';
import { HrManagementComponent } from './hr-management/hr-management.component';
import { FinanceManagementComponent } from './finance-management/finance-management.component';
import {ComplianceAuditComponent} from './compliance-audit/compliance-audit.component';
import { ProcurementComponent } from './procurement/procurement.component';
import { SalesComponent } from './sales/sales.component';
import { BusinessOperationsComponent } from './business-operations/business-operations.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'inventory', component: InventoryComponent },
  { path: 'hr', component: HrManagementComponent },
  { path: 'finance', component: FinanceManagementComponent },
  { path: 'compliance', component: ComplianceAuditComponent },
  { path: 'procurement', component: ProcurementComponent },
  { path: 'sales', component: SalesComponent }
  ,{ path: 'operations', component: BusinessOperationsComponent }
];
