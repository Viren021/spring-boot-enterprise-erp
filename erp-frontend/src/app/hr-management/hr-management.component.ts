import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // REQUIRED FOR FORMS!
import { ApiService } from '../api.service';

@Component({
  selector: 'app-hr-management',
  standalone: true,
  imports: [CommonModule, FormsModule], // ADD FormsModule HERE
  templateUrl: './hr-management.component.html',
  styleUrl: './hr-management.component.scss'
})
export class HrManagementComponent implements OnInit {

  employees: any[] = [];
  isHrAdmin: boolean = false; // Our RBAC security lock

  // Data model for the HTML form
  newEmployee = {
    name: '',
    department: '',
    role: '',
    salary: 0
  };

  constructor(private apiService: ApiService, private cdr: ChangeDetectorRef) {}

  async ngOnInit() {
    // 1. Check if the logged-in user is Alice (HR Admin)
    this.isHrAdmin = this.apiService.hasRole('ROLE_HR_ADMIN');

    // 2. Load the employee directory (Everyone can read this)
    await this.loadEmployees();
  }

  async loadEmployees() {
    try {
      this.employees = await this.apiService.getEmployees() as any[];
      this.cdr.detectChanges();
    } catch (error) {
      console.error("Failed to load employees", error);
    }
  }

  async submitNewEmployee() {
    try {
      console.log("Saving employee:", this.newEmployee);
      await this.apiService.addEmployee(this.newEmployee);

      alert("✅ Employee Successfully Onboarded!");

      // Clear the form and refresh the table
      this.newEmployee = { name: '', department: '', role: '', salary: 0 };
      await this.loadEmployees();

    } catch (error) {
      console.error("Failed to save employee.", error);
      alert("❌ Error: You do not have permission, or the server failed.");
    }
  }
}
