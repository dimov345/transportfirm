import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';

@Component({
  selector: 'app-vehicle-form',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './vehicle-form.html',
  styleUrls: ['./vehicle-form.scss']
})
export class VehicleFormComponent implements OnInit {
  vehicleForm: FormGroup;
  isEdit = false;
  isSubmitting = false;

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

  emissionStandards = [
    'Euro 1', 'Euro 2', 'Euro 3', 'Euro 4', 'Euro 5', 'Euro 6'
  ];

  constructor(
    private fb: FormBuilder,
    private vehicleService: VehicleService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.vehicleForm = this.createForm();
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id') || '';

    if (this.id) {
      this.isEdit = true;

      // не позволявай промяна на plateNumber при edit
      this.vehicleForm.get('plateNumber')?.disable({ emitEvent: false });

      this.loadVehicle();
    }
  }

  createForm(): FormGroup {
    return this.fb.group({
      id: [''], // може да стои (backend може да го игнорира при create)
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

  loadVehicle() {
    this.vehicleService.getById(this.id).subscribe({
      next: (vehicle: VehicleInfo) => {
        this.vehicleForm.patchValue(vehicle);
      },
      error: () => {
        alert('Грешка при зареждане на данните за превозното средство!');
        this.router.navigate(['/vehicles']);
      }
    });
  }

  onSubmit() {
    if (this.vehicleForm.invalid) {
      this.markAllFieldsAsTouched();
      return;
    }

    this.isSubmitting = true;

    // IMPORTANT: включва disabled полета (plateNumber) в payload-а
    const formData = this.vehicleForm.getRawValue();

    const request = this.isEdit
      ? this.vehicleService.update(this.id, formData)
      : this.vehicleService.create(formData);

    request.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigate(['/vehicles']);
      },
      error: () => {
        this.isSubmitting = false;
        alert(`Грешка при ${this.isEdit ? 'редактиране' : 'създаване'} на превозното средство!`);
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
    Object.keys(this.vehicleForm.controls).forEach(key => {
      this.vehicleForm.get(key)?.markAsTouched();
    });
  }
}
