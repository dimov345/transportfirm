import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-first-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './first-login.html',
  styleUrl: './first-login.scss',
})
export class FirstLogin {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private auth = inject(AuthService);

  private readonly baseUrl = 'http://localhost:8080/api';

  loading = false;
  errorMsg = '';
  infoMsg = history?.state?.message || '';

  // Resend OTP state
  resendLoading = false;
  resendInfo = '';
  resendError = '';
  resendCooldown = 0;
  private resendTimer?: any;

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]]
  });

  ngOnInit() {
    const email = this.route.snapshot.queryParamMap.get('email');
    if (email) this.form.patchValue({ email });
  }

  ngOnDestroy() {
    if (this.resendTimer) clearInterval(this.resendTimer);
  }

  submit() {
    this.errorMsg = '';
    this.resendInfo = '';
    this.resendError = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const email = this.form.value.email!.trim();
    const otp = this.form.value.otp!;
    const newPassword = this.form.value.newPassword!;
    const confirmPassword = this.form.value.confirmPassword!;

    if (newPassword !== confirmPassword) {
      this.errorMsg = 'Паролите не съвпадат.';
      return;
    }

    this.loading = true;

    // 1) first-login: сменяме паролата
    this.http.post<void>(`${this.baseUrl}/auth/first-login`, {
      email,
      otp,
      newPassword
    }).subscribe({
      next: () => {
        // 2) след успех: логваме с новата парола
        this.auth.login({ email, password: newPassword }).subscribe({
          next: () => this.router.navigateByUrl('/'),
          error: (err: HttpErrorResponse) => {
            this.errorMsg =
              err?.error?.message ||
              'Паролата е сменена, но login не успя. Опитай от /login.';
            this.router.navigate(['/login'], { queryParams: { email } });
          },
          complete: () => (this.loading = false)
        });
      },
      error: (err: HttpErrorResponse) => {
        this.errorMsg =
          err?.error?.message ||
          'Неуспешна смяна на парола (OTP грешен/изтекъл).';
        this.loading = false;
      }
    });
  }

  resendOtp() {
    this.resendError = '';
    this.resendInfo = '';

    const email = this.form.value.email?.trim();
    if (!email) {
      this.resendError = 'Моля, въведи email, за да изпратим нов OTP.';
      return;
    }

    if (this.resendCooldown > 0 || this.resendLoading) return;

    this.resendLoading = true;

    // /send-reset-otp?email=...
    this.http.post<void>(`${this.baseUrl}/send-reset-otp`, null, {
      params: { email }
    }).subscribe({
      next: () => {
        this.resendInfo = 'Изпратихме нов OTP код.';
        this.startCooldown(60);
        this.resendLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.resendError =
          err?.error?.message || 'Неуспешно изпращане на OTP. Опитай пак.';
        this.resendLoading = false;
      }
    });
  }

  private startCooldown(seconds: number) {
    this.resendCooldown = seconds;

    if (this.resendTimer) clearInterval(this.resendTimer);

    this.resendTimer = setInterval(() => {
      this.resendCooldown -= 1;

      if (this.resendCooldown <= 0) {
        this.resendCooldown = 0;
        clearInterval(this.resendTimer);
        this.resendTimer = undefined;
      }
    }, 1000);
  }
}
