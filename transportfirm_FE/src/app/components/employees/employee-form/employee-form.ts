import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormGroup
} from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';

import {
  AdminService,
  CreateEmployeeRequest,
  JobTitle,
  Role,
  ContractType
} from '../../../core/services/admin.service';

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
  private adminService = inject(AdminService);

  form!: FormGroup;
  saving = false;
  errorMessage = '';

  // Enum options (1:1 с backend)
  jobTitleOptions = Object.values(JobTitle);
  roleOptions = Object.values(Role);
  contractTypeOptions = Object.values(ContractType);

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.form = this.fb.group({

      // лични
      egn: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      name: ['', Validators.required],
      dateOfBirth: ['', Validators.required],
      phone: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],

      // адреси
      addressPermanent: [''],
      addressCurrent: [''],

      // фирмени
      jobTitle: ['', Validators.required],
      role: ['', Validators.required],
      hiredDate: ['', Validators.required],
      contractType: ['', Validators.required],
      salary: [null, Validators.required],
      salaryNeto: [null],
      salaryCurrency: ['', Validators.required],
      workingHours: ['', Validators.required],

      // банкови
      bankName: ['', Validators.required],
      iban: ['', Validators.required],

      // auth
      username: ['', Validators.required]
    });
  }

  private toPayload(): CreateEmployeeRequest {
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
      salary: Number(v.salary),
      salaryNeto: v.salaryNeto ? Number(v.salaryNeto) : undefined,
      salaryCurrency: v.salaryCurrency,
      workingHours: v.workingHours,

      bankName: v.bankName,
      iban: v.iban,

      username: v.username
    };
  }

  submit(): void {
    this.errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = 'Попълнете задължителните полета';
      return;
    }

    this.saving = true;

    this.adminService.createEmployee(this.toPayload())
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: () => this.router.navigate(['/employees']),
        error: err => {
          this.errorMessage = err.error?.message || 'Грешка при създаване на служител';
          console.error(err);
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
