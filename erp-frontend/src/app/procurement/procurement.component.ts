import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-procurement',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './procurement.component.html'
})
export class ProcurementComponent implements OnInit {
  purchaseRequests: any[] = [];
  purchaseOrders: any[] = [];
  error = '';
  busyId: number | null = null;

  constructor(private api: ApiService) {}

  async ngOnInit() {
    await this.reload();
  }

  async reload() {
    try {
      this.error = '';
      [this.purchaseRequests, this.purchaseOrders] = await Promise.all([
        this.api.getPurchaseRequests(),
        this.api.getPurchaseOrders()
      ]);
    } catch {
      this.error = 'Unable to load procurement workflow data.';
    }
  }

  async submit(id: number) {
    await this.runAction(id, () => this.api.submitPurchaseRequest(id));
  }

  async approveRequest(id: number) {
    await this.runAction(id, () => this.api.approvePurchaseRequest(id));
  }

  async approveOrder(id: number) {
    await this.runAction(id, () => this.api.approvePurchaseOrder(id));
  }

  private async runAction(id: number, action: () => Promise<unknown>) {
    try {
      this.busyId = id;
      await action();
      await this.reload();
    } catch {
      this.error = 'The requested status transition was rejected by the workflow rules.';
    } finally {
      this.busyId = null;
    }
  }
}
