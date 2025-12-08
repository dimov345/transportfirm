import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EmployeeService, EmployeePayload } from '../../../core/services/employee';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './employee-form.html',
  styleUrls: ['./employee-form.scss']
})
export class EmployeeForm implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private svc = inject(EmployeeService);

  form!: FormGroup;
  loading = false;
  saving = false;
  isEdit = false;
  employeeId?: number;
  errorMessage = '';

  // If your backend has fixed enum values you can put them here (strings must match backend)
  jobTitleOptions = ['DRIVER', 'MECHANIC', 'ACCOUNTANT', 'DISPATCHER', 'OTHER'];
  employmentStatusOptions = ['ACTIVE', 'INACTIVE', 'SUSPENDED'];
  contractTypeOptions = ['FULL_TIME', 'PART_TIME', 'CONTRACT'];
  roleOptions = ['USER', 'ADMIN', 'MANAGER'];

  ngOnInit(): void {
    this.initForm();
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEdit = true;
      this.employeeId = Number(idParam);
      this.loadEmployee(this.employeeId);
    }
  }

  private initForm() {
    // Covers all fields from backend EmployeePayload
    this.form = this.fb.group({
      egn: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      name: ['', Validators.required],
      dateOfBirth: [''], // ISO date string yyyy-mm-dd

      phone: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],

      addressPermanent: [''],
      addressCurrent: [''],

      jobTitle: ['', Validators.required],
      department: [''],
      hiredDate: ['', Validators.required],
      employmentStatus: ['ACTIVE'],
      firedDate: [''],

      contractType: [''],
      salary: [null],
      salaryNeto: [null],
      salaryCurrency: [''],
      workingHours: [''],

      bankName: [''],
      iban: [''],

      username: [''],
      password: [''],
      role: ['USER']
    });
  }

  private loadEmployee(id: number) {
    this.loading = true;
    this.svc.getById(id)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: emp => {
          // patch values; backend returns dates as ISO strings (assumption)
          this.form.patchValue({
            egn: emp.egn,
            name: emp.name,
            dateOfBirth: emp.dateOfBirth ? this.toInputDate(emp.dateOfBirth) : '',
            phone: emp.phone,
            email: emp.email,
            addressPermanent: emp.addressPermanent || '',
            addressCurrent: emp.addressCurrent || '',
            jobTitle: emp.jobTitle || '',
            hiredDate: emp.hiredDate ? this.toInputDate(emp.hiredDate) : '',
            employmentStatus: emp.employmentStatus || 'ACTIVE',
            firedDate: emp.firedDate ? this.toInputDate(emp.firedDate) : '',
            contractType: emp.contractType || '',
            salary: emp.salary ?? null,
            salaryNeto: emp.salaryNeto ?? null,
            salaryCurrency: emp.salaryCurrency || '',
            workingHours: emp.workingHours || '',
            bankName: emp.bankName || '',
            iban: emp.iban || '',
            username: emp.username || '',
            // Do NOT patch password for security - leave empty for change
            role: emp.role || 'USER'
          });
        },
        error: err => {
          this.errorMessage = 'Грешка при зареждане на служителя';
          console.error(err);
        }
      });
  }

  // convert to yyyy-MM-dd for <input type="date">
  private toInputDate(value?: string | null): string {
    if (!value) return '';
    const d = new Date(value);
    if (isNaN(d.getTime())) return '';
    const year = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${year}-${mm}-${dd}`;
  }

  private toPayload(): EmployeePayload {
    const v = this.form.value;
    // Build payload exactly as backend expects
    return {
      egn: v.egn,
      name: v.name,
      dateOfBirth: v.dateOfBirth || null,

      phone: v.phone,
      email: v.email,

      addressPermanent: v.addressPermanent || null,
      addressCurrent: v.addressCurrent || null,

      jobTitle: v.jobTitle,
      hiredDate: v.hiredDate,
      employmentStatus: v.employmentStatus || null,
      firedDate: v.firedDate || null,

      contractType: v.contractType || null,
      salary: v.salary ?? null,
      salaryNeto: v.salaryNeto ?? null,
      salaryCurrency: v.salaryCurrency || null,
      workingHours: v.workingHours || null,

      bankName: v.bankName || null,
      iban: v.iban || null,

      username: v.username || null,
      password: v.password || null,
      role: v.role || null
    };
  }

  submit() {
    this.errorMessage = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = 'Попълнете задължителните полета.';
      return;
    }

    this.saving = true;
    const payload = this.toPayload();

    if (this.isEdit && this.employeeId) {
      this.svc.update(this.employeeId, payload)
        .pipe(finalize(() => this.saving = false))
        .subscribe({
          next: () => this.router.navigate(['/employees']),
          error: err => {
            this.errorMessage = 'Грешка при обновяване на служителя';
            console.error(err);
          }
        });
    } else {
      this.svc.create(payload)
        .pipe(finalize(() => this.saving = false))
        .subscribe({
          next: () => this.router.navigate(['/employees']),
          error: err => {
            this.errorMessage = 'Грешка при създаване на служителя';
            console.error(err);
          }
        });
    }
  }

  cancel() {
    this.router.navigate(['/employees']);
  }

  // Helpers for template
  field(name: string) {
    return this.form.get(name);
  }
}
