import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core'; // 🌟 1. Added OnDestroy
import { KeycloakService } from 'keycloak-angular';
import { ApiService } from '../api.service';
import { timer, Subscription } from 'rxjs'; // 🌟 2. Import RxJS timer for the background loop

@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {

  username: string | undefined = '';
  dashboardData: any = null;
  complianceLogs: any[] = []; // 🌟 3. Array to hold the new live audit feed

  private pollingSubscription!: Subscription; // 🌟 4. Tracker for the background loop

  constructor(
    private keycloakService: KeycloakService,
    private apiService: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    // First, verify identity and get the user's name
    if (await this.keycloakService.isLoggedIn()) {
      const userProfile = await this.keycloakService.loadUserProfile();
      this.username = userProfile.username;

      // 🌟 THE REAL-TIME ENGINE: Fire immediately (0ms), then every 3 seconds (3000ms)
      this.pollingSubscription = timer(0, 3000).subscribe(async () => {
        await this.fetchLiveMetrics();
      });
    }
  }

  // 🌟 Abstracted the fetching logic to keep ngOnInit clean
  async fetchLiveMetrics() {
    try {
      // 1. Fetch the main dashboard stats (HR, Finance, Inventory)
      this.dashboardData = await this.apiService.getCeoDashboard();

      // 2. Fetch the latest Compliance Audit Logs
      const logs: any = await this.apiService.getAuditLogs();
      // Only keep the 5 most recent logs so the dashboard doesn't get cluttered
      this.complianceLogs = logs.slice(0, 5);

      // 3. Force the UI to update instantly!
      this.cdr.detectChanges();

    } catch (error) {
      console.error("API Call Failed.", error);
    }
  }

  // 🌟 CRITICAL: Stop the loop when the user logs out or leaves the page!
  ngOnDestroy() {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }

  logout() {
    this.keycloakService.logout('http://localhost:4200');
  }
}
