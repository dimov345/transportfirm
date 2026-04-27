import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { DispatcherService } from '../../../core/services/dispatcher.service';
import { Employee } from '../../../core/models/employee/employee.model';

type DispatcherRow = {
  employeeId: string;
  dispatcherInfoId: string | null;
  employee: Employee;
};

@Component({
  standalone: true,
  selector: 'app-dispatcher-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './dispatcher-list.html',
  styleUrls: ['./dispatcher-list.scss']
})
export class DispatcherListComponent implements OnInit {
  dispatchers: DispatcherRow[] = [];
  allDispatchers: DispatcherRow[] = [];
  loading = true;

  filters = {
    search: ''
  };

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  private destroyRef = inject(DestroyRef);

  constructor(
    private dispatcherService: DispatcherService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (!this.isBrowser) return;
    this.loadDispatchers();
  }

  private setLoading(v: boolean) {
    this.loading = v;
    this.cdr.detectChanges();
  }

  loadDispatchers() {
    this.setLoading(true);

    this.dispatcherService.getAllDispatchers().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (employees) => {
        this.allDispatchers = employees.map(e => this.toRow(e));
        this.applyFilters();
        this.setLoading(false);
      },
      error: (err: unknown) => {
        console.error('Error loading dispatchers:', err);
        this.allDispatchers = [];
        this.dispatchers = [];
        this.setLoading(false);
      }
    });
  }

  private toRow(e: Employee): DispatcherRow {
    return {
      employeeId: e.id,
      dispatcherInfoId: e.dispatcherInfo?.id ?? null,
      employee: e
    };
  }

  applyFilters() {
    let result = [...this.allDispatchers];

    if (this.filters.search) {
      const t = this.filters.search.toLowerCase().trim();
      result = result.filter(d =>
        (d.employee?.name || '').toLowerCase().includes(t) ||
        (d.employee?.egn || '').includes(t)
      );
    }

    this.dispatchers = result;
    this.cdr.detectChanges();
  }

  viewDetails(employeeId: string) {
    this.router.navigate(['/dispatchers', employeeId]);
  }

  trackByEmployeeId(index: number, d: DispatcherRow) {
    return d.employeeId;
  }

  getStatusLabel(status: string | null): string {
    const map: Record<string, string> = {
      ACTIVE: 'Активен',
      INACTIVE: 'Неактивен',
      FIRED: 'Уволнен',
      ON_LEAVE: 'В отпуск'
    };
    return status ? (map[status] ?? status) : '—';
  }

  getStatusClass(status: string | null): string {
    const map: Record<string, string> = {
      ACTIVE: 'status-active',
      INACTIVE: 'status-inactive',
      FIRED: 'status-fired',
      ON_LEAVE: 'status-leave'
    };
    return status ? (map[status] ?? '') : '';
  }
}
