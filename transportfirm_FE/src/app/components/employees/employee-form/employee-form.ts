import { Component, OnInit, inject, ChangeDetectorRef, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import {
  AdminService,
  CreateEmployeeRequest,
  JobTitle,
  Role,
  ContractType
} from '../../../core/services/admin.service';

import { Employee } from '../../../core/models/employee/employee.model';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './employee-form.html',
  styleUrls: ['./employee-form.scss']
})
export class EmployeeForm implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private adminService = inject(AdminService);
  private cdr = inject(ChangeDetectorRef);

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  form!: FormGroup;

  saving = false;
  loading = false;
  errorMessage = '';

  employeeId: string | null = null;

  // public (template го ползва)
  loadedEmployee: Employee | null = null;

  get isEdit(): boolean {
    return !!this.employeeId;
  }

  jobTitleOptions = Object.values(JobTitle);
  roleOptions = Object.values(Role);
  contractTypeOptions = Object.values(ContractType);

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
      role: ['', Validators.required],
      hiredDate: ['', Validators.required],
      contractType: ['', Validators.required],
      salary: [null, Validators.required],
      salaryNeto: [null],
      salaryCurrency: ['', Validators.required],
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
      .pipe(finalize(() => {
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
            phone: emp.phone ?? '',
            email: emp.email ?? '',

            addressPermanent: emp.addressPermanent ?? '',
            addressCurrent: emp.addressCurrent ?? '',

            jobTitle: emp.jobTitle ?? '',
            role: emp.role ?? '',
            hiredDate: this.toDateInput(emp.hiredDate),
            contractType: emp.contractType ?? '',

            salary: emp.salary ?? null,
            salaryNeto: emp.salaryNeto ?? null,
            salaryCurrency: emp.salaryCurrency ?? '',
            workingHours: emp.workingHours ?? '',

            bankName: emp.bankName ?? '',
            iban: emp.iban ?? ''
          });

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
      phone: v.phone,
      email: v.email,

      addressPermanent: v.addressPermanent || undefined,
      addressCurrent: v.addressCurrent || undefined,

      jobTitle: v.jobTitle,
      role: v.role,
      hiredDate: v.hiredDate,
      contractType: v.contractType,

      salary: v.salary != null && v.salary !== '' ? Number(v.salary) : 0,
      salaryNeto: v.salaryNeto != null && v.salaryNeto !== '' ? Number(v.salaryNeto) : undefined,
      salaryCurrency: v.salaryCurrency,
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
      phone: v.phone,
      email: v.email,

      addressPermanent: v.addressPermanent || null,
      addressCurrent: v.addressCurrent || null,

      jobTitle: this.loadedEmployee.jobTitle,

      role: v.role,
      hiredDate: v.hiredDate,
      contractType: v.contractType,

      salary: v.salary != null && v.salary !== '' ? Number(v.salary) : 0,
      salaryNeto: v.salaryNeto != null && v.salaryNeto !== '' ? Number(v.salaryNeto) : null,
      salaryCurrency: v.salaryCurrency,
      workingHours: v.workingHours,

      bankName: v.bankName,
      iban: v.iban
    };
  }

  submit(): void {
    this.errorMessage = '';

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
        next: () => this.router.navigate(['/employees']),
        error: (err: any) => {
          this.errorMessage =
            err?.error?.message ||
            (this.isEdit ? 'Грешка при редакция на служител' : 'Грешка при създаване на служител');
          console.error(err);
          this.cdr.detectChanges();
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/employees']);
  }

  field(name: string) {
    return this.form.get(name);
  }
}
