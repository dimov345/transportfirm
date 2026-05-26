import { Component, OnInit, inject, ChangeDetectorRef, PLATFORM_ID, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { invalidateCache } from '../../../core/interceptors/cache.interceptor';
import { HasUnsavedChanges } from '../../../core/guards/unsaved-changes.guard';

import {
  AdminService,
  CreateEmployeeRequest,
  JobTitle,
  Role,
  ContractType
} from '../../../core/services/admin.service';

import { Employee } from '../../../core/models/employee/employee.model';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './employee-form.html',
  styleUrls: ['./employee-form.scss']
})
export class EmployeeForm implements OnInit, HasUnsavedChanges {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private adminService = inject(AdminService);
  private auth = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  private get backRoute(): string {
    return this.route.snapshot.queryParamMap.get('returnUrl')
      ?? (this.auth.hasRole('ADMIN') ? '/employees' : '/manager-staff');
  }

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  private destroyRef = inject(DestroyRef);

  form!: FormGroup;

  saving = false;
  loading = false;
  errorMessage = '';
  /** Detailed field-level errors returned by the backend @Valid handler. */
  validationErrors: string[] = [];

  employeeId: string | null = null;

  /** Country-code prefix for the phone field (default: Bulgaria) */
  phonePrefix = '+359';

  readonly PHONE_PREFIXES = [
    { code: '+359', label: '🇧🇬 +359 (БГ)' },
    { code: '+49',  label: '🇩🇪 +49 (DE)'  },
    { code: '+90',  label: '🇹🇷 +90 (TR)'  },
    { code: '+40',  label: '🇷🇴 +40 (RO)'  },
    { code: '+30',  label: '🇬🇷 +30 (GR)'  },
    { code: '+381', label: '🇷🇸 +381 (RS)' },
    { code: '+44',  label: '🇬🇧 +44 (UK)'  },
    { code: '+1',   label: '🇺🇸 +1 (US)'   }
  ];

  // public (template го ползва)
  loadedEmployee: Employee | null = null;

  get isEdit(): boolean {
    return !!this.employeeId;
  }

  jobTitleOptions = Object.values(JobTitle);
  contractTypeOptions = Object.values(ContractType);

  readonly jobTitleLabels: Record<string, string | undefined> = {
    DRIVER: 'Шофьор', MECHANIC: 'Механик', DISPATCHER: 'Спедитор',
    ACCOUNTANT: 'Счетоводител', MANAGER: 'Мениджър',
    ADMINISTRATOR: 'Администратор'
  };

  private jobTitleToRole(jobTitle: string): Role {
    const map: Record<string, string> = { ADMINISTRATOR: 'ADMIN' };
    return (map[jobTitle] ?? jobTitle) as Role;
  }

  readonly contractTypeLabels: Record<string, string | undefined> = {
    FULL_TIME: 'Пълен работен ден', PART_TIME: 'Непълен работен ден',
    TEMPORARY: 'Временен', INTERNSHIP: 'Стаж', CIVIL_CONTRACT: 'Граждански договор'
  };

  ngOnInit(): void {
    this.initForm();

    // ✅ SSR: не правим HTTP към бекенда (няма auth токен)
    if (!this.isBrowser) {
      return;
    }

    this.loadIfEdit();
  }

  private initForm(): void {
    this.form = this.fb.group({
      egn: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      name: ['', Validators.required],
      dateOfBirth: ['', Validators.required],
      phone: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],

      addressPermanent: [''],
      addressCurrent: [''],

      jobTitle: ['', Validators.required],
      hiredDate: ['', Validators.required],
      contractType: ['', Validators.required],
      salary: [null, Validators.required],
      salaryNeto: [null],
      workingHours: ['', Validators.required],

      bankName: ['', Validators.required],
      iban: ['', Validators.required],

      // само за create
      username: ['', Validators.required]
    });
  }

  private loadIfEdit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.employeeId = id;

    if (!id) {
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.loadedEmployee = null;
    this.cdr.detectChanges();

    this.adminService.getEmployeeById(id)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (emp: Employee) => {
          this.loadedEmployee = emp;

          this.form.patchValue({
            egn: emp.egn ?? '',
            name: emp.name ?? '',
            dateOfBirth: this.toDateInput(emp.dateOfBirth),
            phone: '',           // will be set by parsePhone below
            email: emp.email ?? '',

            addressPermanent: emp.addressPermanent ?? '',
            addressCurrent: emp.addressCurrent ?? '',

            jobTitle: emp.jobTitle ?? '',
            role: emp.role ?? '',
            hiredDate: this.toDateInput(emp.hiredDate),
            contractType: emp.contractType ?? '',

            salary: emp.salary ?? null,
            salaryNeto: emp.salaryNeto ?? null,
            workingHours: emp.workingHours ?? '',

            bankName: emp.bankName ?? '',
            iban: emp.iban ?? ''
          });

          // Parse stored phone into prefix + local
          if (emp.phone) this.parsePhone(emp.phone);

          // edit: username не участва
          this.form.get('username')?.clearValidators();
          this.form.get('username')?.setValue('');
          this.form.get('username')?.updateValueAndValidity();

          // edit: jobTitle е заключен
          this.form.get('jobTitle')?.disable();

          this.form.markAsPristine();
          this.cdr.detectChanges();
        },
        error: (err: any) => {
          if (err?.status === 401) {
            this.errorMessage = 'Нямаш активна сесия. Влез отново.';
            // this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
          } else {
            this.errorMessage = err?.error?.message || 'Грешка при зареждане на служител';
          }
          console.error(err);
          this.cdr.detectChanges();
        }
      });
  }

  /** Splits a stored E.164 phone into prefix + local and updates the form. */
  private parsePhone(stored: string): void {
    if (!stored) return;
    // Sort by length descending to avoid +1 matching +381 first
    const sorted = [...this.PHONE_PREFIXES].sort((a, b) => b.code.length - a.code.length);
    for (const p of sorted) {
      if (stored.startsWith(p.code)) {
        this.phonePrefix = p.code;
        this.form.get('phone')?.setValue(stored.slice(p.code.length));
        return;
      }
    }
    // No known prefix matched — show number as-is
    this.form.get('phone')?.setValue(stored);
  }

  /** Builds the full E.164 phone by combining prefix + local number. */
  private buildPhone(): string {
    const local = (this.form.value.phone || '').replace(/\s/g, '');
    if (local.startsWith('+')) return local;          // already E.164
    const digits = local.replace(/^0+/, '');           // strip leading zero(s)
    return this.phonePrefix + digits;
  }

  private toDateInput(value: any): string {
    if (!value) return '';
    return typeof value === 'string' ? value.slice(0, 10) : '';
  }

  private toCreatePayload(): CreateEmployeeRequest {
    const v = this.form.value;

    return {
      egn: v.egn,
      name: v.name,
      dateOfBirth: v.dateOfBirth,
      phone: this.buildPhone(),
      email: v.email,

      addressPermanent: v.addressPermanent || undefined,
      addressCurrent: v.addressCurrent || undefined,

      jobTitle: v.jobTitle,
      role: this.jobTitleToRole(v.jobTitle),
      hiredDate: v.hiredDate,
      contractType: v.contractType,

      salary: v.salary != null && v.salary !== '' ? Number(v.salary) : 0,
      salaryNeto: v.salaryNeto != null && v.salaryNeto !== '' ? Number(v.salaryNeto) : undefined,
      salaryCurrency: 'EUR',
      workingHours: v.workingHours,

      bankName: v.bankName,
      iban: v.iban,

      username: v.username
    };
  }

  private toUpdatePayload(): Partial<Employee> {
    if (!this.loadedEmployee) throw new Error('Employee not loaded yet');

    const v = this.form.getRawValue();

    return {
      egn: v.egn,
      name: v.name,
      dateOfBirth: v.dateOfBirth,
      phone: this.buildPhone(),
      email: v.email,

      addressPermanent: v.addressPermanent || null,
      addressCurrent: v.addressCurrent || null,

      jobTitle: this.loadedEmployee.jobTitle,

      role: this.jobTitleToRole(this.loadedEmployee.jobTitle),
      hiredDate: v.hiredDate,
      contractType: v.contractType,

      salary: v.salary != null && v.salary !== '' ? Number(v.salary) : 0,
      salaryNeto: v.salaryNeto != null && v.salaryNeto !== '' ? Number(v.salaryNeto) : null,
      salaryCurrency: 'EUR',
      workingHours: v.workingHours,

      bankName: v.bankName,
      iban: v.iban
    };
  }

  submit(): void {
    this.errorMessage = '';
    this.validationErrors = [];

    // ✅ SSR safety: submit само в браузъра
    if (!this.isBrowser) return;

    if (this.isEdit && !this.loadedEmployee) {
      this.errorMessage = 'Данните още се зареждат.';
      this.cdr.detectChanges();
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = 'Попълнете задължителните полета';
      this.cdr.detectChanges();
      return;
    }

    this.saving = true;
    this.cdr.detectChanges();

    const req$: Observable<unknown> = this.isEdit
      ? (this.adminService.updateEmployee(this.employeeId!, this.toUpdatePayload()) as Observable<unknown>)
      : (this.adminService.createEmployee(this.toCreatePayload()) as Observable<unknown>);

    req$
      .pipe(finalize(() => {
        this.saving = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => { invalidateCache(); this.form.markAsPristine(); this.router.navigate([this.backRoute]); },
        error: (err: any) => {
          this.errorMessage =
            err?.error?.message ||
            (this.isEdit ? 'Грешка при редакция на служител' : 'Грешка при създаване на служител');
          this.validationErrors = err?.error?.errors ?? [];
          console.error(err);
          this.cdr.detectChanges();
        }
      });
  }

  cancel(): void {
    this.router.navigate([this.backRoute]);
  }

  field(name: string) {
    return this.form.get(name);
  }


  hasUnsavedChanges(): boolean {
    return this.form.dirty;
  }
}
