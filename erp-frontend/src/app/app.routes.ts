import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { InventoryComponent } from './inventory/inventory.component';
import { HrManagementComponent } from './hr-management/hr-management.component';
import { FinanceManagementComponent } from './finance-management/finance-management.component';
import {ComplianceAuditComponent} from './compliance-audit/compliance-audit.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'inventory', component: InventoryComponent },
  { path: 'hr', component: HrManagementComponent },
  { path: 'finance', component: FinanceManagementComponent },
  { path: 'compliance', component: ComplianceAuditComponent }
];
