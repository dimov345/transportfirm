import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';

import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';
import { invalidateCache } from '../../../core/interceptors/cache.interceptor';
import { HasUnsavedChanges } from '../../../core/guards/unsaved-changes.guard';

@Component({
  selector: 'app-vehicle-form',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './vehicle-form.html',
  styleUrls: ['./vehicle-form.scss']
})
export class VehicleFormComponent implements OnInit, HasUnsavedChanges {
  private fb = inject(FormBuilder);
  private vehicleService = inject(VehicleService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  private destroyRef = inject(DestroyRef);

  vehicleForm: FormGroup = this.createForm();

  isEdit = false;
  isSubmitting = false;
  loading = false;
  errorMessage = '';
  id = '';

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

  // Enum -> BG labels
  vehicleStatuses = [
    { value: 'AVAILABLE', label: 'Свободно' },
    { value: 'ON_ROUTE', label: 'На курс' },
    { value: 'IN_SERVICE', label: 'В сервиз' },
    { value: 'BROKEN_DOWN', label: 'Повредено' },
    { value: 'WAITING_PARTS', label: 'Чака части' },
    { value: 'OUT_OF_SERVICE', label: 'Извън експлоатация' }
  ];

  ngOnInit() {
    if (!this.isBrowser) return;

    this.id = this.route.snapshot.paramMap.get('id') || '';

    if (this.id) {
      this.isEdit = true;
      this.vehicleForm.get('plateNumber')?.disable({ emitEvent: false });
      this.loadVehicle();
    }
  }

  private createForm(): FormGroup {
    return this.fb.group({
      // важно: null, не ''
      id: [null],

      plateNumber: ['', [Validators.required, Validators.maxLength(20)]],
      model: ['', [Validators.required, Validators.maxLength(100)]],
      engineNumber: ['', [Validators.required, Validators.maxLength(50)]],
      chassisNumber: ['', [Validators.required, Validators.maxLength(50)]],
      yearOfManufacture: [null, [Validators.required, Validators.min(1900), Validators.max(2100)]],
      owner: ['', [Validators.required, Validators.maxLength(100)]],
      typePps: ['', Validators.required],
      technicalCondition: ['', Validators.required],
      standardEmissions: ['', Validators.required],

      // New fields
      vehicleStatus: [null], // важно: null, не ''
      odometerKm: [null],
      lastServiceOdometerKm: [null],
      lastServiceDate: [null],
      nextServiceDueOdometerKm: [null],
      nextServiceDueDate: [null],
      avgConsumptionLper100: [null],
      notesForMechanic: [''],

      // Leasing
      leased: [false],
      leasingStartDate: [null],
      leasingEndDate: [null],
      leasingCompany: [''],
      leasingContractNumber: [''],
      leasingMonthlyPaymentEur: [null],

      kaskoOt: [null],
      kaskoDo: [null],
      grazhdanskaOtgovornostOt: [null],
      grazhdanskaOtgovornostDo: [null],
      gtpOt: [null],
      gtpDo: [null],
      vinetkaOt: [null],
      vinetkaDo: [null]
    });
  }

  private loadVehicle() {
    this.loading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    this.vehicleService
      .getById(this.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (vehicle: VehicleInfo) => {
          // ако backend връща "" за неща (рядко) - нормализираме към null
          this.vehicleForm.patchValue({
            ...vehicle,
            vehicleStatus: (vehicle as any)?.vehicleStatus || null,
            lastServiceDate: (vehicle as any)?.lastServiceDate || null,
            nextServiceDueDate: (vehicle as any)?.nextServiceDueDate || null,
            kaskoOt: (vehicle as any)?.kaskoOt || null,
            kaskoDo: (vehicle as any)?.kaskoDo || null,
            grazhdanskaOtgovornostOt: (vehicle as any)?.grazhdanskaOtgovornostOt || null,
            grazhdanskaOtgovornostDo: (vehicle as any)?.grazhdanskaOtgovornostDo || null,
            gtpOt: (vehicle as any)?.gtpOt || null,
            gtpDo: (vehicle as any)?.gtpDo || null,
            vinetkaOt: (vehicle as any)?.vinetkaOt || null,
            vinetkaDo: (vehicle as any)?.vinetkaDo || null,
            leasingStartDate: (vehicle as any)?.leasingStartDate || null,
            leasingEndDate: (vehicle as any)?.leasingEndDate || null,
          });

          this.vehicleForm.markAsPristine();
          this.cdr.detectChanges();
        },
        error: (err: any) => {
          this.errorMessage = err?.error?.message || 'Грешка при зареждане на данните за превозното средство!';
          console.error(err);
          this.cdr.detectChanges();
          this.router.navigate(['/vehicles']);
        }
      });
  }

  onSubmit() {
    this.errorMessage = '';
    if (!this.isBrowser) return;

    if (this.vehicleForm.invalid) {
      this.markAllFieldsAsTouched();
      this.errorMessage = 'Попълнете задължителните полета.';
      this.cdr.detectChanges();
      return;
    }

    this.isSubmitting = true;
    this.cdr.detectChanges();

    const raw = this.vehicleForm.getRawValue();

    // ===== Build payload safely =====
    const payload: any = { ...raw };

    // при create махаме празни полета, за да не гръмнат UUID/enum
    if (!payload.id) delete payload.id;
    if (!payload.vehicleStatus) delete payload.vehicleStatus;

    // date empty -> null
    const dateKeys = [
      'kaskoOt','kaskoDo',
      'grazhdanskaOtgovornostOt','grazhdanskaOtgovornostDo',
      'gtpOt','gtpDo',
      'vinetkaOt','vinetkaDo',
      'leasingStartDate','leasingEndDate',
      'lastServiceDate','nextServiceDueDate'
    ];
    for (const k of dateKeys) {
      if (payload[k] === '') payload[k] = null;
    }

    const request = this.isEdit
      ? this.vehicleService.update(this.id, payload)
      : this.vehicleService.create(payload);

    request
      .pipe(
        finalize(() => {
          this.isSubmitting = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => { invalidateCache(); this.vehicleForm.markAsPristine(); this.router.navigate(['/vehicles']); },
        error: (err: any) => {
          this.errorMessage =
            err?.error?.message || `Грешка при ${this.isEdit ? 'редактиране' : 'създаване'} на превозното средство!`;
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
    if (field.errors['maxlength']) return `Максимална дължина е ${field.errors['maxlength'].requiredLength} символа`;

    return 'Невалидна стойност';
  }

  private markAllFieldsAsTouched() {
    Object.keys(this.vehicleForm.controls).forEach((key) => {
      this.vehicleForm.get(key)?.markAsTouched();
    });
  }

  getVehicleStatusLabel(value?: string | null): string {
    if (!value) return '—';
    return this.vehicleStatuses.find(s => s.value === value)?.label ?? value;
  }

  hasUnsavedChanges(): boolean {
    return this.vehicleForm.dirty;
  }
}
