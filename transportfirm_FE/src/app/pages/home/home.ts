import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

type NavCard = {
  label:    string;
  subtitle: string;
  icon:     string;
  route:    string;
  accent:   string;
};

@Component({
  selector:    'app-home',
  standalone:  true,
  imports:     [CommonModule, RouterModule],
  templateUrl: './home.html',
  styleUrl:    './home.scss'
})
export class Home {
  private auth = inject(AuthService);

  readonly email = this.auth.getEmail() ?? '—';
  readonly role  = this.auth.getRole()  ?? '—';

  readonly roleLabels: Record<string, string> = {
    ADMIN:      'Администратор',
    MANAGER:    'Мениджър',
    DISPATCHER: 'Спедитор',
    MECHANIC:   'Механик',
    DRIVER:     'Шофьор'
  };

  private readonly allCards: Record<string, NavCard[]> = {
    ADMIN: [
      {
        label:    'Шофьори',
        subtitle: 'Списък на всички шофьори и документи',
        icon:     'drive_eta',
        route:    '/drivers',
        accent:   '#2563eb'
      },
      {
        label:    'ППС',
        subtitle: 'Управление на превозни средства',
        icon:     'directions_car',
        route:    '/vehicles',
        accent:   '#0f766e'
      },
      {
        label:    'Механици',
        subtitle: 'Списък на механиците и групи',
        icon:     'build',
        route:    '/mechanics',
        accent:   '#b45309'
      },
      {
        label:    'Спедитори',
        subtitle: 'Списък на спедиторите и групи',
        icon:     'local_shipping',
        route:    '/dispatchers',
        accent:   '#7c3aed'
      }
    ],
    MANAGER: [
      {
        label:    'Шофьори',
        subtitle: 'Списък на всички шофьори и документи',
        icon:     'drive_eta',
        route:    '/drivers',
        accent:   '#2563eb'
      },
      {
        label:    'ППС',
        subtitle: 'Управление на превозни средства',
        icon:     'directions_car',
        route:    '/vehicles',
        accent:   '#0f766e'
      },
      {
        label:    'Механици',
        subtitle: 'Списък на механиците и групи',
        icon:     'build',
        route:    '/mechanics',
        accent:   '#b45309'
      },
      {
        label:    'Спедитори',
        subtitle: 'Списък на спедиторите и групи',
        icon:     'local_shipping',
        route:    '/dispatchers',
        accent:   '#7c3aed'
      }
    ],
    DISPATCHER: [
      {
        label:    'Моите ППС',
        subtitle: 'Превозни средства от вашите групи',
        icon:     'local_shipping',
        route:    '/dispatcher',
        accent:   '#2563eb'
      },
      {
        label:    'Шофьори',
        subtitle: 'Преглед на шофьори и документи',
        icon:     'drive_eta',
        route:    '/drivers',
        accent:   '#0f766e'
      },
      {
        label:    'Всички ППС',
        subtitle: 'Преглед на целия автопарк',
        icon:     'directions_car',
        route:    '/vehicles',
        accent:   '#7c3aed'
      }
    ],
    MECHANIC: [
      {
        label:    'Моите ППС',
        subtitle: 'Поддръжка на назначените превозни средства',
        icon:     'build',
        route:    '/mechanic',
        accent:   '#b45309'
      }
    ],
    DRIVER: [
      {
        label:    'Профил',
        subtitle: 'Вашата лична информация',
        icon:     'person',
        route:    '/profile',
        accent:   '#2563eb'
      }
    ]
  };

  get roleLabel(): string    { return this.roleLabels[this.role] ?? this.role; }
  get cards():     NavCard[] { return this.allCards[this.role]   ?? [];        }

  get greeting(): string {
    const h = new Date().getHours();
    if (h >= 5  && h < 12) return 'Добро утро';
    if (h >= 12 && h < 18) return 'Добър ден';
    if (h >= 18 && h < 22) return 'Добър вечер';
    return 'Добра нощ';
  }

  get todayLabel(): string {
    return new Date().toLocaleDateString('bg-BG', {
      weekday: 'long',
      day:     'numeric',
      month:   'long',
      year:    'numeric'
    });
  }

  get initials(): string {
    if (this.email === '—') return '?';
    const local = this.email.split('@')[0];
    const parts = local.split(/[._-]/);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return local.substring(0, 2).toUpperCase();
  }
}
