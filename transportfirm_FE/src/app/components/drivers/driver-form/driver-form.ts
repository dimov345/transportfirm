import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DriverService } from '../../../core/services/driver';
import { DriverInfo } from '../../../core/services/driver';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-driver-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './driver-form.html',
  styleUrls: ['./driver-form.scss']
})
export class DriverFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  id!: number;

  constructor(
    private fb: FormBuilder,
    private driverService: DriverService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.form = this.fb.group({
      employeeId: [null, Validators.required],
      driverLicenseIssuedOn: ['', Validators.required],
      driverLicenseExpiresOn: ['', Validators.required],
      qualificationCardIssuedOn: ['', Validators.required],
      qualificationCardExpiresOn: ['', Validators.required],
      psychologicalExamIssuedOn: ['', Validators.required],
      psychologicalExamExpiresOn: ['', Validators.required],
      digitalCardIssuedOn: ['', Validators.required],
      digitalCardExpiresOn: ['', Validators.required],
    });

    // ВЗИМАМЕ id от URL
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.isEdit = true;

      // Зареждаме данните
      this.driverService.getDriverById(this.id).subscribe(driver => {
         this.form.patchValue({
            employeeId: driver.employee?.id ?? null,  // 🔥 важен ред
            ...driver
        });
      });
    }
  }

  onSubmit() {
    const raw = this.form.getRawValue();
     const payload: any = {
      ...raw,
      employee: { id: raw.employeeId }  // 🔥 задължително
    };

    delete payload.employeeId;

    if (this.isEdit) {
      this.driverService.update(this.id, payload).subscribe(() => {
        this.router.navigate(['/drivers']);
      });
    } else {
      this.driverService.create(payload).subscribe(() => {
        this.router.navigate(['/drivers']);
      });
    }
  }
}
