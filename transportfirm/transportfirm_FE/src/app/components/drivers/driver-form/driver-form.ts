import { Component, OnInit, inject, PLATFORM_ID, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

import { DriverService } from '../../../core/services/driver.service';
import { Employee } from '../../../core/models/employee/employee.model';
import { DriverInfo } from '../../../core/models/driver/driver-info.model';
import { invalidateCache } from '../../../core/interceptors/cache.interceptor';
import { HasUnsavedChanges } from '../../../core/guards/unsaved-changes.guard';

@Component({
  selector: 'app-driver-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './driver-form.html',
  styleUrls: ['./driver-form.scss']
})
export class DriverFormComponent implements OnInit, HasUnsavedChanges {
  form!: FormGroup;

  employeeId!: string;
  driverInfoId: string | null = null;

  isEdit = true;     // ✅ за твоя HTML (няма create в този бекенд)
  loading = true;
  saving = false;

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  private destroyRef = inject(DestroyRef);

  constructor(
    private fb: FormBuilder,
    private driverService: DriverService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.form = this.fb.group({
      driverLicenseIssuedOn: ['', Validators.required],
      driverLicenseExpiresOn: ['', Validators.required],
      qualificationCardIssuedOn: ['', Validators.required],
      qualificationCardExpiresOn: ['', Validators.required],
      psychologicalExamIssuedOn: ['', Validators.required],
      psychologicalExamExpiresOn: ['', Validators.required],
      digitalCardIssuedOn: ['', Validators.required],
      digitalCardExpiresOn: ['', Validators.required],
    });

    if (!this.isBrowser) return;

    const employeeIdParam = this.route.snapshot.paramMap.get('id');
    if (!employeeIdParam) {
      this.router.navigate(['/drivers']);
      return;
    }

    this.employeeId = employeeIdParam;

    // ✅ GET employee/{employeeId}
    this.driverService.getEmployee(this.employeeId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (employee: Employee) => {
        const di: DriverInfo | null | undefined = employee.driverInfo;

        // ако DriverInfo трябва винаги да има — това е реален проблем в данните
        if (!di) {
          this.loading = false;
          alert('Няма DriverInfo за този служител. (Трябва да се създава при createEmployee.)');
          this.router.navigate(['/drivers']);
          return;
        }

        this.driverInfoId = di.id;
        this.loading = false;

        this.form.patchValue({
          driverLicenseIssuedOn: di.driverLicenseIssuedOn ?? '',
          driverLicenseExpiresOn: di.driverLicenseExpiresOn ?? '',
          qualificationCardIssuedOn: di.qualificationCardIssuedOn ?? '',
          qualificationCardExpiresOn: di.qualificationCardExpiresOn ?? '',
          psychologicalExamIssuedOn: di.psychologicalExamIssuedOn ?? '',
          psychologicalExamExpiresOn: di.psychologicalExamExpiresOn ?? '',
          digitalCardIssuedOn: di.digitalCardIssuedOn ?? '',
          digitalCardExpiresOn: di.digitalCardExpiresOn ?? '',
        });
        this.form.markAsPristine();
      },
      error: (err: unknown) => {
        console.error('Failed to load employee:', err);
        this.loading = false;
        this.router.navigate(['/drivers']);
      }
    });
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (!this.isBrowser) return;

    if (!this.driverInfoId) {
      alert('Липсва driverInfoId – няма как да се запише.');
      return;
    }

    this.saving = true;

    const payload: Partial<DriverInfo> = {
      ...this.form.getRawValue(),
      id: this.driverInfoId
    };

    this.driverService.updateDriverInfo(this.driverInfoId, payload).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.saving = false;
        invalidateCache();
        this.form.markAsPristine();
        this.router.navigate(['/drivers']);
      },
      error: (err: unknown) => {
        this.saving = false;
        console.error('Update failed:', err);
        alert('Грешка при запис!');
      }
    });
  }

  hasUnsavedChanges(): boolean {
    return this.form.dirty;
  }
}
