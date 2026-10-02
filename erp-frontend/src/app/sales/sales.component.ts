import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-sales',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sales.component.html'
})
export class SalesComponent implements OnInit {
  customers: any[] = [];
  orders: any[] = [];
  error = '';

  constructor(private api: ApiService) {}

  async ngOnInit() {
    try {
      [this.customers, this.orders] = await Promise.all([
        this.api.getCustomers() as Promise<any[]>,
        this.api.getSalesOrders() as Promise<any[]>
      ]);
    } catch {
      this.error = 'Unable to load order-to-cash data.';
    }
  }
}
