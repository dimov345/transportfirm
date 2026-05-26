import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy, inject, PLATFORM_ID, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { EmployeeService, EmployeeListItem } from '../../../core/services/employee.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule],
  templateUrl: './employee-list.html',
  styleUrls: ['./employee-list.scss']
})
export class EmployeeList implements OnInit {
  readonly jobTitleLabels: Record<string, string | undefined> = {
    DRIVER: 'Шофьор', MECHANIC: 'Механик', DISPATCHER: 'Спедитор',
    ACCOUNTANT: 'Счетоводител', MANAGER: 'Мениджър',
    ADMINISTRATOR: 'Администратор'
  };

  employees: EmployeeListItem[] = [];
  filtered: EmployeeListItem[] = [];
  searchText = '';
  loading = true;

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  private destroyRef = inject(DestroyRef);
  private authService = inject(AuthService);

  get isAdmin(): boolean { return this.authService.hasRole('ADMIN'); }

  constructor(
    private employeeService: EmployeeService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (!this.isBrowser) return; // SSR-safe
    this.loadEmployees();
  }

  private setLoading(v: boolean) {
    this.loading = v;
    this.cdr.detectChanges();
  }

  loadEmployees() {
    this.setLoading(true);

    this.employeeService.getAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => {
        this.employees = data || [];
        this.filtered = this.employees;
        this.setLoading(false);
      },
      error: (err) => {
        console.error('Error loading employees:', err);
        this.setLoading(false);
      }
    });
  }

  search() {
    const text = this.searchText.toLowerCase().trim();

    if (!text) {
      this.filtered = this.employees;
      this.cdr.detectChanges();
      return;
    }

    this.filtered = this.employees.filter(e =>
      (e.name || '').toLowerCase().includes(text) ||
      (e.egn || '').includes(text) ||
      (e.jobTitle || '').toLowerCase().includes(text)
    );

    this.cdr.detectChanges();
  }

  createEmployee() {
    this.router.navigate(['/employees/new']);
  }

  editEmployee(id: string) {
    this.router.navigate([`/employees/edit/${id}`]);
  }

  viewDetails(id: string) {
    this.router.navigate([`/employees/details/${id}`]);
  }

  viewDocuments(id: string) {
    this.router.navigate([`/employees-documents/${id}`]);
  }

  deleteEmployee(id: string) {
    if (!confirm('Сигурен ли си, че искаш да изтриеш този служител?')) return;

    this.employeeService.delete(id).subscribe({
      next: () => {
        this.employees = this.employees.filter(e => e.id !== id);
        this.search();
      },
      error: (err) => {
        console.error('Error deleting employee:', err);
        alert(err?.error?.message || 'Грешка при изтриване на служителя!');
      }
    });
  }

  trackByEmployeeId(index: number, emp: any) {
  return emp.id;
}
}
