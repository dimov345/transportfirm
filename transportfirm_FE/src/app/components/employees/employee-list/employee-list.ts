import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeeService, Employee, EmployeeListItem } from '../../../core/services/employee.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employee-list.html',
  styleUrls: ['./employee-list.scss']
})
export class EmployeeList implements OnInit {

  employees: EmployeeListItem[] = [];
  filtered: EmployeeListItem[] = [];

  searchText: string = '';

  constructor(private employeeService: EmployeeService, private router: Router,  private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees() {
    this.employeeService.getAll().subscribe(data => {
      this.employees = data;
      this.filtered = data;
      this.cdr.detectChanges();
    });
  }

  search() {
    const text = this.searchText.toLowerCase().trim();

    this.filtered = this.employees.filter(e =>
      e.name.toLowerCase().includes(text) ||
      e.egn.includes(text) ||
      (e.jobTitle && e.jobTitle.toLowerCase().includes(text))
    );
  }

  createEmployee() {
    this.router.navigate(['/employees/new']);
  }

  editEmployee(id: number) {
    this.router.navigate([`/employees/form/${id}`]);
  }

  viewDetails(id: number) {
    this.router.navigate([`/employees/details/${id}`]);
  }

  viewDocuments(id: number) {
    this.router.navigate([`/employees/documents/${id}`]);
  }

  deleteEmployee(id: number) {
    if (!confirm('Сигурен ли си, че искаш да изтриеш този служител?')) return;

    this.employeeService.delete(id).subscribe(() => {
      this.employees = this.employees.filter(e => e.id !== id);
      this.search();
    });
  }
}
