import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';

import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';

@Component({
  selector: 'app-vehicle-form',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './vehicle-form.html',
  styleUrls: ['./vehicle-form.scss']
})
export class VehicleFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private vehicleService = inject(VehicleService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  vehicleForm: FormGroup = this.createForm();

  isEdit = false;
  isSubmitting = false;
  loading = false;
  errorMessage = '';

  id = ''; // UUID от route

  vehicleTypes = [
    'Лек автомобил',
    'Автобус',
    'Лекотоварен автомобил (до 3.5 т)',
    'Тежкотоварен камион',
    'Автоплатформен и специализиран камион (бетоновози, самосвали и др.)',
    'Линейка',
    'Пожарна кола',
    'Полицейски автомобил',
    'Снегорини, почистваща техника и др.',
    'Ремаркета и полуремаркета',
    'Влекачи (тягови ППС за ремаркета)'
  ];

  technicalConditions = [
    'В движение',
    'С временно отстранени неизправности',
    'Спряно от движение',
    'Повредено',
    'Бракувано'
  ];

  emissionStandards = ['Euro 1', 'Euro 2', 'Euro 3', 'Euro 4', 'Euro 5', 'Euro 6'];

  ngOnInit() {
    // ✅ SSR guard: не правим HTTP и не ползваме browser APIs
    if (!this.isBrowser) return;

    this.id = this.route.snapshot.paramMap.get('id') || '';

    if (this.id) {
      this.isEdit = true;

      // не позволявай промяна на plateNumber при edit
      this.vehicleForm.get('plateNumber')?.disable({ emitEvent: false });

      this.loadVehicle();
    }
  }

  private createForm(): FormGroup {
    return this.fb.group({
      id: [''],
      plateNumber: ['', [Validators.required, Validators.maxLength(20)]],
      model: ['', [Validators.required, Validators.maxLength(100)]],
      engineNumber: ['', [Validators.required, Validators.maxLength(50)]],
      chassisNumber: ['', [Validators.required, Validators.maxLength(50)]],
      yearOfManufacture: ['', [Validators.required, Validators.min(1900), Validators.max(2100)]],
      owner: ['', [Validators.required, Validators.maxLength(100)]],
      typePps: ['', Validators.required],
      technicalCondition: ['', Validators.required],
      standardEmissions: ['', Validators.required],
      kaskoOt: [''],
      kaskoDo: [''],
      grazhdanskaOtgovornostOt: [''],
      grazhdanskaOtgovornostDo: [''],
      gtpOt: [''],
      gtpDo: [''],
      vinetkaOt: [''],
      vinetkaDo: ['']
    });
  }

  private loadVehicle() {
    this.loading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    this.vehicleService.getById(this.id)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (vehicle: VehicleInfo) => {
          this.vehicleForm.patchValue(vehicle);
          this.vehicleForm.markAsPristine();
          this.cdr.detectChanges();
        },
        error: (err: any) => {
          // ✅ вместо alert -> UI message (SSR-safe)
          this.errorMessage =
            err?.error?.message || 'Грешка при зареждане на данните за превозното средство!';
          console.error(err);
          this.cdr.detectChanges();

          // по желание: върни към списъка
          this.router.navigate(['/vehicles']);
        }
      });
  }

  onSubmit() {
    this.errorMessage = '';

    // ✅ SSR safety
    if (!this.isBrowser) return;

    if (this.vehicleForm.invalid) {
      this.markAllFieldsAsTouched();
      this.errorMessage = 'Попълнете задължителните полета.';
      this.cdr.detectChanges();
      return;
    }

    this.isSubmitting = true;
    this.cdr.detectChanges();

    // включва disabled полета (plateNumber) в payload-а
    const formData = this.vehicleForm.getRawValue();

    const request = this.isEdit
      ? this.vehicleService.update(this.id, formData)
      : this.vehicleService.create(formData);

    request
      .pipe(finalize(() => {
        this.isSubmitting = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => this.router.navigate(['/vehicles']),
        error: (err: any) => {
          this.errorMessage =
            err?.error?.message ||
            `Грешка при ${this.isEdit ? 'редактиране' : 'създаване'} на превозното средство!`;
          console.error(err);
          this.cdr.detectChanges();
        }
      });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.vehicleForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.vehicleForm.get(fieldName);
    if (!field || !field.errors) return '';

    if (field.errors['required']) return 'Това поле е задължително';
    if (field.errors['min']) return `Минимална стойност е ${field.errors['min'].min}`;
    if (field.errors['max']) return `Максимална стойност е ${field.errors['max'].max}`;
    if (field.errors['maxlength'])
      return `Максимална дължина е ${field.errors['maxlength'].requiredLength} символа`;

    return 'Невалидна стойност';
  }

  private markAllFieldsAsTouched() {
    Object.keys(this.vehicleForm.controls).forEach(key => {
      this.vehicleForm.get(key)?.markAsTouched();
    });
  }
}
