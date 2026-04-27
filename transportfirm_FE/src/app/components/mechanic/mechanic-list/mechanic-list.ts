import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy, inject, PLATFORM_ID, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { MechanicService } from '../../../core/services/mechanic.service';
import { Employee } from '../../../core/models/employee/employee.model';

type MechanicRow = {
  employeeId: string;
  mechanicInfoId: string | null;
  employee: Employee;
  hasGroups: boolean;
};

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-mechanic-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './mechanic-list.html',
  styleUrls: ['./mechanic-list.scss']
})
export class MechanicListComponent implements OnInit {
  mechanics: MechanicRow[] = [];
  allMechanics: MechanicRow[] = [];
  loading = true;

  filters = {
    search: ''
  };

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  private destroyRef = inject(DestroyRef);

  constructor(
    private mechanicService: MechanicService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (!this.isBrowser) return;
    this.loadMechanics();
  }

  private setLoading(v: boolean) {
    this.loading = v;
    this.cdr.detectChanges();
  }

  loadMechanics() {
    this.setLoading(true);

    this.mechanicService.getAllMechanics().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (employees) => {
        this.allMechanics = employees.map(e => this.toRow(e));
        this.applyFilters();
        this.setLoading(false);
      },
      error: (err: unknown) => {
        console.error('Error loading mechanics:', err);
        this.allMechanics = [];
        this.mechanics = [];
        this.setLoading(false);
      }
    });
  }

  private toRow(e: Employee): MechanicRow {
    return {
      employeeId: e.id,
      mechanicInfoId: e.mechanicInfo?.id ?? null,
      employee: e,
      hasGroups: false
    };
  }

  applyFilters() {
    let result = [...this.allMechanics];

    if (this.filters.search) {
      const t = this.filters.search.toLowerCase().trim();
      result = result.filter(m =>
        (m.employee?.name || '').toLowerCase().includes(t) ||
        (m.employee?.egn || '').includes(t)
      );
    }

    this.mechanics = result;
    this.cdr.detectChanges();
  }

  viewDetails(employeeId: string) {
    this.router.navigate(['/mechanics', employeeId]);
  }

  trackByEmployeeId(index: number, m: MechanicRow) {
    return m.employeeId;
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
