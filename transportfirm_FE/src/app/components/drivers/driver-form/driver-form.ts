import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DriverService, DriverInfo } from '../../../core/services/driver';
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
  egn!: string;

  constructor(
    private fb: FormBuilder,
    private driverService: DriverService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    // създаваме формата тук, когато fb вече е наличен
    this.form = this.fb.group({
      egn: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      name: ['', Validators.required],
      phone: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      driverLicenseIssuedOn: ['', Validators.required],
      driverLicenseExpiresOn: ['', Validators.required],
      qualificationCardIssuedOn: ['', Validators.required],
      qualificationCardExpiresOn: ['', Validators.required],
      psychologicalExamIssuedOn: [''],
      psychologicalExamExpiresOn: [''],
      digitalCardIssuedOn: [''],
      digitalCardExpiresOn: ['']
    });

    this.egn = this.route.snapshot.paramMap.get('egn')!;
    if (this.egn) {
      this.isEdit = true;
      this.driverService.getByEgn(this.egn).subscribe(driver => this.form.patchValue(driver));
      this.form.get('egn')?.disable();
    }
  }

  onSubmit() {
    const value = this.form.getRawValue() as DriverInfo;
    if (this.isEdit) {
      this.driverService.update(this.egn, value).subscribe(() => this.router.navigate(['/drivers']));
    } else {
      this.driverService.create(value).subscribe(() => this.router.navigate(['/drivers']));
    }
  }
}
