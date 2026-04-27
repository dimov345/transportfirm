import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

type QuickLink = { label: string; icon: string; route: string };

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.scss']
})
export class ProfileComponent {
  private auth   = inject(AuthService);
  private router = inject(Router);

  readonly email = this.auth.getEmail() ?? '—';
  readonly role  = this.auth.getRole()  ?? '—';

  readonly roleLabels: Record<string, string> = {
    ADMIN:      'Администратор',
    MANAGER:    'Мениджър',
    DISPATCHER: 'Спедитор',
    MECHANIC:   'Механик',
    DRIVER:     'Шофьор'
  };

  readonly roleDescriptions: Record<string, string> = {
    ADMIN:      'Пълен достъп до всички модули на системата',
    MANAGER:    'Управление на служители, ППС и групи',
    DISPATCHER: 'Преглед на ППС и шофьори в назначените групи',
    MECHANIC:   'Управление на техническа поддръжка за ППС',
    DRIVER:     'Достъп до лична информация и задания'
  };

  readonly roleIcons: Record<string, string> = {
    ADMIN:      'admin_panel_settings',
    MANAGER:    'manage_accounts',
    DISPATCHER: 'local_shipping',
    MECHANIC:   'build',
    DRIVER:     'drive_eta'
  };

  readonly roleColors: Record<string, string> = {
    ADMIN:      '#7c3aed',
    MANAGER:    '#0f766e',
    DISPATCHER: '#2563eb',
    MECHANIC:   '#b45309',
    DRIVER:     '#15803d'
  };

  private readonly allLinks: Record<string, QuickLink[]> = {
    ADMIN: [
      { label: 'Механици',   icon: 'build',          route: '/mechanics'   },
      { label: 'Спедитори',  icon: 'local_shipping', route: '/dispatchers' },
      { label: 'ППС',        icon: 'directions_car', route: '/vehicles'    },
      { label: 'Шофьори',   icon: 'drive_eta',       route: '/drivers'     }
    ],
    MANAGER: [
      { label: 'Механици',   icon: 'build',          route: '/mechanics'   },
      { label: 'Спедитори',  icon: 'local_shipping', route: '/dispatchers' },
      { label: 'ППС',        icon: 'directions_car', route: '/vehicles'    },
      { label: 'Шофьори',   icon: 'drive_eta',       route: '/drivers'     }
    ],
    DISPATCHER: [
      { label: 'Моите ППС', icon: 'local_shipping',  route: '/dispatcher'  },
      { label: 'Шофьори',  icon: 'drive_eta',        route: '/drivers'     },
      { label: 'Всички ППС', icon: 'directions_car', route: '/vehicles'    }
    ],
    MECHANIC: [
      { label: 'Моите ППС', icon: 'build',            route: '/mechanic'   }
    ],
    DRIVER: []
  };

  // ---- computed ----
  get roleLabel():       string    { return this.roleLabels[this.role]      ?? this.role;    }
  get roleDescription(): string    { return this.roleDescriptions[this.role] ?? '';          }
  get roleIcon():        string    { return this.roleIcons[this.role]        ?? 'person';    }
  get roleColor():       string    { return this.roleColors[this.role]       ?? '#3182ce';   }
  get links():           QuickLink[] { return this.allLinks[this.role]       ?? [];          }

  get initials(): string {
    if (this.email === '—') return '?';
    const local = this.email.split('@')[0];
    const parts = local.split(/[._-]/);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return local.substring(0, 2).toUpperCase();
  }

  // ---- JWT session info (iat/exp stored at login, token itself is httpOnly) ----

  get tokenIssuedAt(): string {
    const v = this.auth.getTokenIat();
    if (v === null) return '—';
    return new Date(v * 1000).toLocaleString('bg-BG');
  }

  get tokenExpiresAt(): string {
    const v = this.auth.getTokenExp();
    if (v === null) return '—';
    return new Date(v * 1000).toLocaleString('bg-BG');
  }

  get tokenDaysLeft(): number | null {
    const v = this.auth.getTokenExp();
    if (v === null) return null;
    return Math.ceil((v * 1000 - Date.now()) / 86_400_000);
  }

  get tokenStatusClass(): string {
    const d = this.tokenDaysLeft;
    if (d === null || d > 1) return 'token-ok';
    if (d > 0)               return 'token-warn';
    return 'token-expired';
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
