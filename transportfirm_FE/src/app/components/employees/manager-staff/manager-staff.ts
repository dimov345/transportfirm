import {
  Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef,
  inject, PLATFORM_ID, DestroyRef
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EmployeeService, EmployeeListItem } from '../../../core/services/employee.service';

@Component({
  selector: 'app-manager-staff',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule],
  templateUrl: './manager-staff.html',
  styleUrls: ['./manager-staff.scss']
})
export class ManagerStaffComponent implements OnInit {

  private cdr        = inject(ChangeDetectorRef);
  private router     = inject(Router);
  private empSvc     = inject(EmployeeService);
  private platformId = inject(PLATFORM_ID);
  private destroyRef = inject(DestroyRef);

  employees: EmployeeListItem[] = [];
  filtered:  EmployeeListItem[] = [];
  searchText = '';
  loading = true;

  readonly jobTitleLabels: Record<string, string> = {
    DRIVER: 'Шофьор', MECHANIC: 'Механик', DISPATCHER: 'Спедитор',
    ACCOUNTANT: 'Счетоводител', MANAGER: 'Мениджър',
    ADMINISTRATOR: 'Администратор'
  };

  readonly jobTitleColors: Record<string, string> = {
    DRIVER:        '#2563eb',
    MECHANIC:      '#b45309',
    DISPATCHER:    '#7c3aed',
    ACCOUNTANT:    '#0f766e',
    MANAGER:       '#1d4ed8',
    ADMINISTRATOR: '#4b5563'
  };

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.empSvc.getAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: data => {
        this.employees = data ?? [];
        this.applyFilter();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  search(): void {
    this.applyFilter();
    this.cdr.detectChanges();
  }

  private applyFilter(): void {
    const q = this.searchText.toLowerCase().trim();
    this.filtered = q
      ? this.employees.filter(e =>
          e.name.toLowerCase().includes(q) ||
          (e.jobTitle ?? '').toLowerCase().includes(q) ||
          (e.phone ?? '').includes(q) ||
          (e.email ?? '').toLowerCase().includes(q))
      : [...this.employees];
  }

  viewDetails(id: string): void { this.router.navigate([`/employees/details/${id}`]); }
  editEmployee(id: string): void { this.router.navigate([`/employees/edit/${id}`]); }

  /** Two-letter initials from full name. */
  initials(name: string): string {
    if (!name) return '?';
    const parts = name.trim().split(/\s+/);
    return parts.length >= 2
      ? (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
      : name.substring(0, 2).toUpperCase();
  }

  colorFor(jobTitle: string): string {
    return this.jobTitleColors[jobTitle] ?? '#4b5563';
  }

  labelFor(jobTitle: string): string {
    return this.jobTitleLabels[jobTitle] ?? jobTitle;
  }

  trackById = (_: number, e: EmployeeListItem) => e.id;
}
