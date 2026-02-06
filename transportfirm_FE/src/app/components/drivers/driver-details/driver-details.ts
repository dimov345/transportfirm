import { Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { catchError, of } from 'rxjs';

import { DriverService } from '../../../core/services/driver.service';
import { Employee } from '../../../core/models/employee/employee.model';
import { DriverInfo } from '../../../core/models/driver/driver-info.model';

@Component({
  selector: 'app-driver-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-details.html',
  styleUrls: ['./driver-details.scss']
})
export class DriverDetails implements OnInit {
  employee: Employee | null = null;
  driverInfo: DriverInfo | null = null;

  error = '';
  loading = true;

  employeeId!: string;

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private driverService: DriverService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    // SSR safe
    if (!this.isBrowser) return;

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.error = 'Не е посочен ID на шофьора';
      this.loading = false;
      return;
    }

    this.employeeId = idParam;
    this.loadDriverDetails();
  }

  loadDriverDetails() {
    this.loading = true;
    this.error = '';

    this.driverService.getEmployee(this.employeeId).pipe(
      catchError((err: unknown) => {
        console.error(err);
        this.error = 'Грешка при зареждане на данните за шофьора';
        this.loading = false;
        return of(null);
      })
    ).subscribe((emp) => {
      if (!emp) return;

      this.employee = emp;
      this.driverInfo = emp.driverInfo ?? null;

      // ако за този employee няма driverInfo -> показваме грешка/empty
      if (!this.driverInfo) {
        this.error = 'Този служител няма DriverInfo (няма срокове за документи).';
      }

      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  // ---------------- DOCUMENT STATUS ------------------

  private getExpiryDates(): (string | null)[] {
    if (!this.driverInfo) return [];
    return [
      this.driverInfo.driverLicenseExpiresOn,
      this.driverInfo.qualificationCardExpiresOn,
      this.driverInfo.psychologicalExamExpiresOn,
      this.driverInfo.digitalCardExpiresOn
    ];
  }

  get hasExpiredDocuments(): boolean {
    const today = new Date();
    return this.getExpiryDates().some(d => d && new Date(d) < today);
  }

  get expiredDocumentsCount(): number {
    const today = new Date();
    return this.getExpiryDates().filter(d => d && new Date(d) < today).length;
  }

  get expiringDocumentsCount(): number {
    const today = new Date();
    const in30Days = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000);

    return this.getExpiryDates().filter(date => {
      if (!date) return false;
      const expiry = new Date(date);
      return expiry > today && expiry <= in30Days;
    }).length;
  }

  isExpired(date: string | null): boolean {
    return !!date && new Date(date) < new Date();
  }

  isExpiring(date: string | null): boolean {
    if (!date) return false;
    const expiry = new Date(date);
    const today = new Date();
    const in30Days = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000);
    return expiry > today && expiry <= in30Days;
  }

  getDocumentStatus(date: string | null): string {
    if (!date) return 'Няма данни';
    if (this.isExpired(date)) return 'Изтекъл';
    if (this.isExpiring(date)) return 'Изтича скоро';
    return 'Валиден';
  }

  formatDate(date: string | null) {
    return date ? new Date(date).toLocaleDateString('bg-BG') : '—';
  }

  // ---------------- ACTIONS ------------------

  goBack() {
    this.router.navigate(['/drivers']);
  }

  editDriver() {
    // edit е по employeeId (както при list/form)
    this.router.navigate(['/drivers/edit', this.employeeId]);
  }

  viewDocuments() {
    // документите ти в контролера са по driverInfoId
    if (!this.driverInfo?.id) return;
    this.router.navigate(['/driver-documents', this.driverInfo.id]);
  }
}
