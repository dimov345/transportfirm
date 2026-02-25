import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.scss']
})
export class ProfileComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  email = this.auth.getEmail() ?? '—';
  role = this.auth.getRole() ?? '—';

  readonly roleLabels: Record<string, string> = {
    ADMIN: 'Администратор',
    MANAGER: 'Мениджър',
    DISPATCHER: 'Спедитор',
    MECHANIC: 'Механик',
    DRIVER: 'Шофьор'
  };

  get roleLabel(): string {
    return this.roleLabels[this.role] ?? this.role;
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
