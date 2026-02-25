import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { DriverService } from '../../../core/services/driver.service';
import { Employee } from '../../../core/models/employee/employee.model';
import { DriverInfo } from '../../../core/models/driver/driver-info.model';
import { AuthService } from '../../../core/auth/auth.service';

type DriverRow = {
  id: string | null;

  employeeId: string;

  employee: Employee;

  driverLicenseIssuedOn: string | null;
  driverLicenseExpiresOn: string | null;

  qualificationCardIssuedOn: string | null;
  qualificationCardExpiresOn: string | null;

  psychologicalExamIssuedOn: string | null;
  psychologicalExamExpiresOn: string | null;

  digitalCardIssuedOn: string | null;
  digitalCardExpiresOn: string | null;

  statusClass: string;
  statusTooltip: string;
};

@Component({
  standalone: true,
  selector: 'app-drivers-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './drivers-list.html',
  styleUrls: ['./drivers-list.scss']
})
export class DriversListComponent implements OnInit {
  drivers: DriverRow[] = [];
  allDrivers: DriverRow[] = [];
  loading = true;

  filters = {
    search: '',
    status: '',
    expiringIn: '',
    email: ''
  };

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  private auth = inject(AuthService);

  get isDispatcher(): boolean {
    return this.auth.hasRole('DISPATCHER');
  }

  constructor(
    private driverService: DriverService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (!this.isBrowser) return;
    this.loadDrivers();
  }

  private setLoading(v: boolean) {
    this.loading = v;
    this.cdr.detectChanges();
  }

  loadDrivers() {
    this.setLoading(true);

    this.driverService.getAllDriversFromEmployee().subscribe({
      next: (employees) => {
        const rows = employees.map(e => this.toRow(e));
        this.allDrivers = this.markStatuses(rows);
        this.applyFilters();
        this.setLoading(false);
      },
      error: (err: unknown) => {
        console.error('Error loading drivers:', err);
        this.allDrivers = [];
        this.drivers = [];
        this.setLoading(false);
      }
    });
  }

  // --------- mapping Employee -> DriverRow (за да не пипаме HTML/CSS) ---------
  private toRow(e: Employee): DriverRow {
    const di: DriverInfo | null | undefined = e.driverInfo;

    return {
      id: di?.id ?? null,
      employeeId: e.id,
      employee: e,

      driverLicenseIssuedOn: di?.driverLicenseIssuedOn ?? null,
      driverLicenseExpiresOn: di?.driverLicenseExpiresOn ?? null,

      qualificationCardIssuedOn: di?.qualificationCardIssuedOn ?? null,
      qualificationCardExpiresOn: di?.qualificationCardExpiresOn ?? null,

      psychologicalExamIssuedOn: di?.psychologicalExamIssuedOn ?? null,
      psychologicalExamExpiresOn: di?.psychologicalExamExpiresOn ?? null,

      digitalCardIssuedOn: di?.digitalCardIssuedOn ?? null,
      digitalCardExpiresOn: di?.digitalCardExpiresOn ?? null,

      statusClass: '',
      statusTooltip: ''
    };
  }

  // ----------------- helpers for status -----------------
  private hasAnyDocs(d: DriverRow): boolean {
    return !!(
      d.driverLicenseExpiresOn ||
      d.qualificationCardExpiresOn ||
      d.psychologicalExamExpiresOn ||
      d.digitalCardExpiresOn
    );
  }

  private isExpiredRow(d: DriverRow): boolean {
    const now = new Date();
    return [
      d.driverLicenseExpiresOn,
      d.qualificationCardExpiresOn,
      d.psychologicalExamExpiresOn,
      d.digitalCardExpiresOn
    ].some(date => !!date && new Date(date) < now);
  }

  private isExpiringInDays(d: DriverRow, days: number): boolean {
    const now = new Date();
    const limit = new Date(now.getTime() + days * 86400000);

    return [
      d.driverLicenseExpiresOn,
      d.qualificationCardExpiresOn,
      d.psychologicalExamExpiresOn,
      d.digitalCardExpiresOn
    ].some(date => {
      if (!date) return false;
      const dt = new Date(date);
      return dt > now && dt <= limit;
    });
  }

  private isExpiringRow(d: DriverRow): boolean {
    return this.isExpiringInDays(d, 30);
  }

  private markStatuses(rows: DriverRow[]): DriverRow[] {
    return rows.map(d => {
      const hasDocs = this.hasAnyDocs(d);
      const expired = hasDocs && this.isExpiredRow(d);
      const expiring = hasDocs && !expired && this.isExpiringRow(d);

      if (expired) {
        d.statusClass = 'expired-row';
        d.statusTooltip = 'Документ е изтекъл';
      } else if (expiring) {
        d.statusClass = 'expiring-row';
        d.statusTooltip = 'Изтичащ документ';
      } else {
        d.statusClass = '';
        d.statusTooltip = '';
      }
      return d;
    });
  }

  // ----------------- filters (за твоето HTML) -----------------
  applyFilters() {
    let result = [...this.allDrivers];

    if (this.filters.search) {
      const t = this.filters.search.toLowerCase().trim();
      result = result.filter(d =>
        (d.employee?.name || '').toLowerCase().includes(t) ||
        (d.employee?.egn || '').includes(t)
      );
    }

    if (this.filters.status) {
      result = result.filter(d => {
        const hasDocs = this.hasAnyDocs(d);

        switch (this.filters.status) {
          case 'missing': return !hasDocs;
          case 'expired': return hasDocs && this.isExpiredRow(d);
          case 'expiring': return hasDocs && this.isExpiringRow(d);
          case 'valid': return hasDocs && !this.isExpiredRow(d) && !this.isExpiringRow(d);
          default: return true;
        }
      });
    }

    if (this.filters.expiringIn) {
      const days = Number(this.filters.expiringIn);
      result = result.filter(d => this.isExpiringInDays(d, days));
    }

    if (this.filters.email) {
      result = result.filter(d =>
        this.filters.email === 'yes'
          ? !!d.employee?.email
          : !d.employee?.email
      );
    }

    this.drivers = result;
    this.cdr.detectChanges();
  }

  // ----------------- actions used by твоето HTML -----------------
  addDriverInfo(d: DriverRow) {
    this.router.navigate(['/drivers/edit', d.employeeId]);
  }

  driverDetail(employeeId: string) {
    this.router.navigate(['/drivers', employeeId]);
  }

  viewDocuments(employeeId: string) {
    // ако документите ти са по employeeId (както беше в HTML)
    this.router.navigate(['/driver-documents', employeeId]);
    // ако са по DriverInfo.id: смени на this.router.navigate(['/driver-documents', d.id])
  }

  trackByEmployeeId(index: number, d: DriverRow) {
    return d.employeeId;
  }

  exportCSV() {
    console.log('Export CSV not implemented yet.');
  }
}
