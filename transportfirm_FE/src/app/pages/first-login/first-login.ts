import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../core/auth/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-first-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './first-login.html',
  styleUrl: './first-login.scss',
})
export class FirstLogin {
  private fb    = inject(FormBuilder);
  private http  = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private auth  = inject(AuthService);

  private readonly baseUrl = environment.apiUrl;

  loading  = false;
  errorMsg = '';
  infoMsg  = history?.state?.message || '';

  form = this.fb.group({
    email:           ['', [Validators.required, Validators.email]],
    otp:             ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
    newPassword:     ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]]
  });

  ngOnInit() {
    const email = this.route.snapshot.queryParamMap.get('email');
    if (email) this.form.patchValue({ email });
  }

  submit() {
    this.errorMsg = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const email           = this.form.value.email!.trim();
    const otp             = this.form.value.otp!;
    const newPassword     = this.form.value.newPassword!;
    const confirmPassword = this.form.value.confirmPassword!;

    if (newPassword !== confirmPassword) {
      this.errorMsg = 'Паролите не съвпадат.';
      return;
    }

    this.loading = true;

    this.http.post<void>(`${this.baseUrl}/auth/first-login`, { email, otp, newPassword })
      .subscribe({
        next: () => {
          this.auth.login({ email, password: newPassword }).subscribe({
            next: () => this.router.navigateByUrl('/'),
            error: (err: HttpErrorResponse) => {
              this.errorMsg =
                err?.error?.message ||
                'Паролата е сменена, но входът не успя. Опитай от /login.';
              this.router.navigate(['/login'], { queryParams: { email } });
            },
            complete: () => (this.loading = false)
          });
        },
        error: (err: HttpErrorResponse) => {
          this.errorMsg =
            err?.error?.message ||
            'Неуспешна смяна на парола — OTP е грешен.';
          this.loading = false;
        }
      });
  }

  get passwordMismatch(): boolean {
    const { newPassword, confirmPassword } = this.form.value;
    return !!(newPassword && confirmPassword && newPassword !== confirmPassword);
  }
}
