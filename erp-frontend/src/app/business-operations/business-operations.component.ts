import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-business-operations',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './business-operations.component.html'
})
export class BusinessOperationsComponent implements OnInit {
  tab = 'finance';
  error = '';
  busy = false;
  accounts: any[] = [];
  periods: any[] = [];
  journals: any[] = [];
  masterData: any[] = [];
  payrollPeriods: any[] = [];
  payrollRuns: any[] = [];
  leaves: any[] = [];
  expenses: any[] = [];
  resource = 'departments';
  newAccount: any = { code: '', name: '', type: 'ASSET' };
  newMasterValue: any = { name: '', code: '' };
  newPayrollPeriod: any = { name: '', startDate: '', endDate: '' };
  newPayrollRun: any = { periodId: null };

  constructor(private api: ApiService) {}

  async ngOnInit() { await this.loadFinance(); }
  async select(tab: string) { this.tab = tab; this.error = ''; if (tab === 'finance') await this.loadFinance(); if (tab === 'master') await this.loadMaster(); if (tab === 'payroll') await this.loadPayroll(); if (tab === 'approvals') await this.loadApprovals(); }
  private async loadFinance() { try { [this.accounts, this.periods, this.journals] = await Promise.all([this.api.getAccounts(), this.api.getFiscalPeriods(), this.api.getJournals()]) as any[][]; } catch { this.error = 'Finance data could not be loaded.'; } }
  async addAccount() { await this.run(() => this.api.createAccount(this.newAccount)); this.newAccount = { code: '', name: '', type: 'ASSET' }; await this.loadFinance(); }
  async addPeriod() { await this.run(() => this.api.createFiscalPeriod(this.newPayrollPeriod)); this.newPayrollPeriod = { name: '', startDate: '', endDate: '' }; await this.loadFinance(); }
  async postJournal(id: string) { await this.run(() => this.api.postJournal(id)); await this.loadFinance(); }
  async loadMaster() { try { this.masterData = await this.api.getMasterData(this.resource) as any[]; } catch { this.error = 'Master data could not be loaded.'; } }
  async addMasterValue() { await this.run(() => this.api.createMasterData(this.resource, this.newMasterValue)); this.newMasterValue = { name: '', code: '' }; await this.loadMaster(); }
  async loadPayroll() { try { [this.payrollPeriods, this.payrollRuns] = await Promise.all([this.api.getPayrollPeriods(), this.api.getPayrollRuns()]) as any[][]; } catch { this.error = 'Payroll data could not be loaded.'; } }
  async addPayrollPeriod() { await this.run(() => this.api.createPayrollPeriod(this.newPayrollPeriod)); await this.loadPayroll(); }
  async addPayrollRun() { await this.run(() => this.api.createPayrollRun(this.newPayrollRun)); await this.loadPayroll(); }
  async processRun(id: number) { await this.run(() => this.api.processPayrollRun(id)); await this.loadPayroll(); }
  async loadApprovals() { try { [this.leaves, this.expenses] = await Promise.all([this.api.getApprovalLeaves(), this.api.getApprovalExpenses()]) as any[][]; } catch { this.error = 'Approval queues could not be loaded.'; } }
  async approveLeave(id: number) { await this.run(() => this.api.approveLeaveRequest(id)); await this.loadApprovals(); }
  async rejectLeave(id: number) { await this.run(() => this.api.rejectLeaveRequest(id, 'Rejected by approver')); await this.loadApprovals(); }
  async approveExpense(id: number) { await this.run(() => this.api.approveExpenseClaim(id)); await this.loadApprovals(); }
  async rejectExpense(id: number) { await this.run(() => this.api.rejectExpenseClaim(id, 'Rejected by approver')); await this.loadApprovals(); }
  private async run(action: () => Promise<unknown>) { this.busy = true; this.error = ''; try { await action(); } catch { this.error = 'The API rejected this operation.'; } finally { this.busy = false; } }
}
