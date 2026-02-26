import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loading = false;
  errorMsg = '';

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  ngOnInit() {
    // ако идваш обратно към login с prefilled email
    const email = this.route.snapshot.queryParamMap.get('email');
    if (email) this.form.patchValue({ email });
  }

  submit() {
    this.errorMsg = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    const email = this.form.value.email!.trim();
    const password = this.form.value.password!;

    this.auth.login({ email, password }).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err: HttpErrorResponse) => {
        // backend: 403 + firstLoginRequired=true
        const firstLoginRequired =
          err?.status === 403 &&
          (err?.error?.firstLoginRequired === true || err?.error?.firstLoginRequired === 'true');

        if (firstLoginRequired) {
          // Интерцепторът вече прави redirect. Тук само спираме loading.
          // Ако по някаква причина интерцепторът не е вързан -> fallback redirect оттук:
          const msg =
            err?.error?.message || 'Първо влизане: трябва да смениш паролата, преди да продължиш.';

          this.router.navigate(['/first-login'], {
            queryParams: { email },
            state: { message: msg }
          });

          this.loading = false;
          return;
        }

        this.errorMsg = err?.error?.message || 'Грешен email или парола.';
        this.loading = false;
      },
      complete: () => (this.loading = false)
    });
  }
}
