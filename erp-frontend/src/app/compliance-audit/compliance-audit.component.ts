import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; // 🌟 1. Import ChangeDetectorRef
import { CommonModule } from '@angular/common';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-compliance-audit',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './compliance-audit.component.html',
  styleUrls: ['./compliance-audit.component.scss']
})
export class ComplianceAuditComponent implements OnInit {

  auditLogs: any[] = [];
  isLoading: boolean = true;
  errorMessage: string = '';

  constructor(
    private apiService: ApiService,
    private cdr: ChangeDetectorRef // 🌟 2. Inject it into the constructor
  ) {}

  async ngOnInit() {
    await this.loadAuditLogs();
  }

  async loadAuditLogs() {
    try {
      this.isLoading = true;
      this.errorMessage = '';

      const response: any = await this.apiService.getAuditLogs();

      if (response && Array.isArray(response)) {
        this.auditLogs = response;

        // 🌟 3. MANUALLY TELL ANGULAR TO REDRAW THE HTML TABLE!
        this.cdr.detectChanges();
      }

    } catch (error) {
      console.error('Failed to load audit logs:', error);
      this.errorMessage = 'Unable to connect to the Compliance Service.';
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges(); // Also redraw when loading finishes
    }
  }
}
