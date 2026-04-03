import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-finance-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './finance-management.component.html',
  styleUrl: './finance-management.component.scss'
})
export class FinanceManagementComponent implements OnInit {

  transactions: number[] = [];
  isFinanceAdmin: boolean = false;

  newTransaction = {
    amount: null as number | null,
    description: '',
    type: 'Expense'
  };

  constructor(private apiService: ApiService, private cdr: ChangeDetectorRef) {}

  async ngOnInit() {
    // 1. Check if they are in the Finance Department
    this.isFinanceAdmin = this.apiService.hasRole('ROLE_FINANCE_ADMIN');

    // 2. Load the Ledger
    await this.loadLedger();
  }

  async loadLedger() {
    try {
      // Fetching the list of doubles from our real PostgreSQL backend
      this.transactions = await this.apiService.getLedger() as number[];

      // Using ChangeDetectorRef is a great practice here to ensure the UI updates!
      this.cdr.detectChanges();
    } catch (error) {
      console.error("Failed to load ledger", error);
    }
  }

  async submitTransaction() {
    // 🌟 ADDED VALIDATION: Prevent empty or negative submissions
    if (!this.newTransaction.amount || this.newTransaction.amount <= 0) {
      alert("❌ Please enter a valid positive amount.");
      return;
    }

    try {
      console.log("Saving transaction:", this.newTransaction);

      // Send the data to your Spring Boot Controller
      await this.apiService.addTransaction(this.newTransaction);

      alert("✅ Transaction Successfully Recorded in PostgreSQL!");

      // Clear the form fields
      this.newTransaction = { amount: null, description: '', type: 'Expense' };

      // Refresh the table to show the new transaction immediately
      await this.loadLedger();

    } catch (error) {
      console.error("Failed to save transaction.", error);
      alert("❌ Error: You do not have permission, or the backend refused the connection.");
    }
  }
}
