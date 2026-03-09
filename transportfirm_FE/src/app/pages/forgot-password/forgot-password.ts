import { Component, computed, inject, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../../environments/environment';

type Step = 1 | 2 | 3 | 4;   // 1=email  2=OTP  3=new password  4=success

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss',
})
export class ForgotPassword implements OnDestroy {
  private fb     = inject(FormBuilder);
  private http   = inject(HttpClient);
  private router = inject(Router);

  private readonly baseUrl = environment.apiUrl;

  step = signal<Step>(1);

  // Per-step state
  loadingEmail    = signal(false);
  loadingPassword = signal(false);
  errorMsg        = signal('');

  // Stored across steps
  private sentEmail  = '';
  private enteredOtp = '';

  // OTP expiry countdown (15 min = 900 s)
  countdownSec = signal(0);
  private countdownTimer?: ReturnType<typeof setInterval>;

  // Resend cooldown (60 s)
  resendCooldown = signal(0);
  resendLoading  = signal(false);
  private resendTimer?: ReturnType<typeof setInterval>;

  // Forms
  emailForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  otpForm = this.fb.group({
    otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
  });

  passwordForm = this.fb.group({
    newPassword:     ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]]
  });

  ngOnDestroy() {
    this.clearTimers();
  }

  // ── Step 1: send OTP ─────────────────────────────────
  sendOtp() {
    this.errorMsg.set('');
    if (this.emailForm.invalid) { this.emailForm.markAllAsTouched(); return; }

    this.loadingEmail.set(true);
    const email = this.emailForm.value.email!.trim();

    this.http.post<void>(`${this.baseUrl}/send-reset-otp`, null, { params: { email } })
      .subscribe({
        next: () => {
          this.sentEmail = email;
          this.loadingEmail.set(false);
          this.startCountdown(15 * 60);   // 15 min
          this.startResendCooldown(60);
          this.step.set(2);
        },
        error: (err: HttpErrorResponse) => {
          this.errorMsg.set(err?.error?.message || 'Неуспешно изпращане. Провери имейла.');
          this.loadingEmail.set(false);
        }
      });
  }

  // ── Step 2: confirm OTP ───────────────────────────────
  confirmOtp() {
    this.errorMsg.set('');
    if (this.otpForm.invalid) { this.otpForm.markAllAsTouched(); return; }

    this.enteredOtp = this.otpForm.value.otp!;
    this.clearCountdown();
    this.step.set(3);
  }

  // ── Step 3: set new password ──────────────────────────
  resetPassword() {
    this.errorMsg.set('');
    if (this.passwordForm.invalid) { this.passwordForm.markAllAsTouched(); return; }

    const { newPassword, confirmPassword } = this.passwordForm.value;
    if (newPassword !== confirmPassword) {
      this.errorMsg.set('Паролите не съвпадат.');
      return;
    }

    this.loadingPassword.set(true);

    this.http.post<void>(`${this.baseUrl}/reset-password`, {
      email:       this.sentEmail,
      otp:         this.enteredOtp,
      newPassword: newPassword!
    }).subscribe({
      next: () => {
        this.loadingPassword.set(false);
        this.step.set(4);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMsg.set(err?.error?.message || 'Неуспешна смяна — OTP е грешен или изтекъл.');
        this.loadingPassword.set(false);
        this.step.set(2);
        this.otpForm.reset();
      }
    });
  }

  // ── Resend OTP ────────────────────────────────────────
  resendOtp() {
    if (this.resendCooldown() > 0 || this.resendLoading()) return;
    this.errorMsg.set('');
    this.resendLoading.set(true);

    this.http.post<void>(`${this.baseUrl}/send-reset-otp`, null, {
      params: { email: this.sentEmail }
    }).subscribe({
      next: () => {
        this.resendLoading.set(false);
        this.startCountdown(15 * 60);
        this.startResendCooldown(60);
        this.otpForm.reset();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMsg.set(err?.error?.message || 'Неуспешно повторно изпращане.');
        this.resendLoading.set(false);
      }
    });
  }

  // ── Helpers ───────────────────────────────────────────
  get email() { return this.sentEmail; }

  countdownLabel = computed((): string => {
    const m = Math.floor(this.countdownSec() / 60);
    const s = this.countdownSec() % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  });

  countdownExpired = computed((): boolean => this.countdownSec() <= 0);

  passwordMismatch = computed((): boolean => {
    const { newPassword, confirmPassword } = this.passwordForm.value;
    return !!(newPassword && confirmPassword && newPassword !== confirmPassword);
  });

  goBackToEmail() {
    this.clearTimers();
    this.otpForm.reset();
    this.step.set(1);
  }

  goToLogin() { this.router.navigate(['/login']); }

  private startCountdown(seconds: number) {
    this.clearCountdown();
    this.countdownSec.set(seconds);
    this.countdownTimer = setInterval(() => {
      this.countdownSec.set(Math.max(0, this.countdownSec() - 1));
      if (this.countdownSec() === 0) this.clearCountdown();
    }, 1000);
  }

  private clearCountdown() {
    if (this.countdownTimer) { clearInterval(this.countdownTimer); this.countdownTimer = undefined; }
  }

  private startResendCooldown(seconds: number) {
    this.clearResendTimer();
    this.resendCooldown.set(seconds);
    this.resendTimer = setInterval(() => {
      this.resendCooldown.set(Math.max(0, this.resendCooldown() - 1));
      if (this.resendCooldown() === 0) this.clearResendTimer();
    }, 1000);
  }

  private clearResendTimer() {
    if (this.resendTimer) { clearInterval(this.resendTimer); this.resendTimer = undefined; }
  }

  private clearTimers() {
    this.clearCountdown();
    this.clearResendTimer();
  }
}
