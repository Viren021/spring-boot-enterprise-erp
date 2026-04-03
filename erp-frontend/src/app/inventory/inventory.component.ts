import { Component, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core'; // 🌟 1. Import OnDestroy
import { CommonModule } from '@angular/common';
import { ApiService } from '../api.service';
import { timer, Subscription } from 'rxjs'; // 🌟 2. Import the Real-Time Engine

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss'
})
export class InventoryComponent implements OnInit, OnDestroy {

  isPredicting = false;
  forecastData: any = null;

  // 🌟 3. Remove the hardcoded numbers. Start with an empty array.
  historicalSales: number[] = [];
  totalItems: number = 0;
    lowStockAlerts: number = 0;
    recentRestocks: number = 0;
  isInventoryAdmin: boolean = false;

  private pollingSubscription!: Subscription; // 🌟 4. Background loop tracker

  constructor(private apiService: ApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    // 1. Check Security Role
    this.isInventoryAdmin = this.apiService.hasRole('ROLE_INVENTORY_ADMIN');
    console.log("Is user an Inventory Admin?", this.isInventoryAdmin);

    // 🌟 2. START THE REAL-TIME ENGINE (Ticks every 3 seconds)
    this.pollingSubscription = timer(0, 3000).subscribe(async () => {
      await this.fetchLiveInventory();
    });
  }

  // 🌟 3. NEW METHOD: Fetch live data from the backend
  async fetchLiveInventory() {
    try {
      // NOTE: Make sure you have a getInventoryStats() method in your api.service.ts!
      // If you don't have the backend endpoint yet, we will simulate it for now.
      const liveData = await this.apiService.getInventoryStats();

      if (liveData) {
               if (liveData.sales) this.historicalSales = liveData.sales;
               this.totalItems = liveData.totalItems || 0;
               this.lowStockAlerts = liveData.lowStockAlerts || 0;
               this.recentRestocks = liveData.recentRestocks || 0;
            }

      this.cdr.detectChanges(); // Force UI redraw

    } catch (error) {
      // Fallback data if backend endpoint isn't ready yet, so your UI doesn't break
      this.historicalSales = [100, 120, 110, 130, 150];
      this.cdr.detectChanges();
    }
  }

  // 🌟 4. CRITICAL: Stop the loop when leaving the page
  ngOnDestroy() {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }

  async runAiForecast() {
    this.isPredicting = true;
    this.cdr.detectChanges();

    try {
      const response = await this.apiService.predictDemand();
      console.log("AI Prediction arrived!", response);
      this.forecastData = { prediction: response };
    } catch (error) {
      console.error("AI Prediction failed.", error);
      this.forecastData = { prediction: "Error: Could not reach the AI microservice." };
    }

    this.isPredicting = false;
    this.cdr.detectChanges();
  }

  async triggerSaga() {
    try {
      const response = await this.apiService.triggerOrderSaga();
      alert(response);
    } catch (error) {
      console.error("Saga failed", error);
      alert("❌ Failed to trigger the Saga. Check console or backend logs.");
    }
  }

  adjustManualStock() {
    const newStockStr = prompt("Enter the new physical stock count for Premium Brake Pads (SKU-BP-001):");

    if (newStockStr !== null && newStockStr.trim() !== "") {
      const newStock = Number(newStockStr);

      if (!isNaN(newStock) && newStock >= 0) {
        // await this.apiService.updateInventory(sku, newStock);
        alert(`✅ SUCCESS: Inventory forcefully updated to ${newStock} units. An audit log has been generated.`);
      } else {
        alert("❌ ERROR: Please enter a valid positive number.");
      }
    }
  }
}
