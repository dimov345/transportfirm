import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, EMPTY } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardStats, DashboardNotification } from '../../core/models/dashboard.model';

type NavCard = { label: string; subtitle: string; icon: string; route: string; accent: string; };

interface DonutSegment { path: string; color: string; label: string; pct: number; value: number; }
interface BarGroupData  { x: number; monthLabel: string; revH: number; expH: number; revY: number; expY: number; revX: number; expX: number; barW: number; netProfit: number; }
interface LinePoint     { x: number; y: number; value: number; monthLabel: string; }

@Component({
  selector:    'app-home',
  standalone:  true,
  imports:     [CommonModule, RouterModule],
  templateUrl: './home.html',
  styleUrl:    './home.scss'
})
export class Home implements OnInit {
  private auth       = inject(AuthService);
  private dashSvc    = inject(DashboardService);
  private router     = inject(Router);
  private cdr        = inject(ChangeDetectorRef);
  private platformId = inject(PLATFORM_ID);

  readonly email = this.auth.getEmail() ?? '—';
  readonly role  = this.auth.getRole()  ?? '—';

  stats:         DashboardStats | null = null;
  notifications: DashboardNotification[] = [];
  statsLoading   = false;
  notifLoading   = false;
  statsError     = '';
  notifError     = '';

  private readonly numFmt = new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  readonly roleLabels: Record<string, string> = {
    ADMIN: 'Администратор', MANAGER: 'Мениджър',
    DISPATCHER: 'Спедитор', MECHANIC: 'Механик', DRIVER: 'Шофьор'
  };

  private readonly allCards: Record<string, NavCard[]> = {
    ADMIN: [
      { label: 'Шофьори',   subtitle: 'Списък на всички шофьори и документи',       icon: 'drive_eta',      route: '/drivers',     accent: '#2563eb' },
      { label: 'ППС',       subtitle: 'Управление на превозни средства',             icon: 'directions_car', route: '/vehicles',    accent: '#0f766e' },
      { label: 'Механици',  subtitle: 'Списък на механиците и групи',                icon: 'build',          route: '/mechanics',   accent: '#b45309' },
      { label: 'Спедитори', subtitle: 'Списък на спедиторите и групи',               icon: 'local_shipping', route: '/dispatchers', accent: '#7c3aed' }
    ],
    MANAGER: [
      { label: 'Шофьори',   subtitle: 'Списък на всички шофьори и документи',       icon: 'drive_eta',      route: '/drivers',     accent: '#2563eb' },
      { label: 'ППС',       subtitle: 'Управление на превозни средства',             icon: 'directions_car', route: '/vehicles',    accent: '#0f766e' },
      { label: 'Механици',  subtitle: 'Списък на механиците и групи',                icon: 'build',          route: '/mechanics',   accent: '#b45309' },
      { label: 'Спедитори', subtitle: 'Списък на спедиторите и групи',               icon: 'local_shipping', route: '/dispatchers', accent: '#7c3aed' }
    ],
    DISPATCHER: [
      { label: 'Моите ППС',  subtitle: 'Превозни средства от вашите групи',          icon: 'local_shipping', route: '/dispatcher',  accent: '#2563eb' },
      { label: 'Шофьори',   subtitle: 'Преглед на шофьори и документи',             icon: 'drive_eta',      route: '/drivers',     accent: '#0f766e' },
      { label: 'Всички ППС', subtitle: 'Преглед на целия автопарк',                  icon: 'directions_car', route: '/vehicles',    accent: '#7c3aed' }
    ],
    MECHANIC: [
      { label: 'Моите ППС', subtitle: 'Поддръжка на назначените превозни средства',  icon: 'build',          route: '/mechanic',    accent: '#b45309' }
    ],
    DRIVER: [
      { label: 'Профил', subtitle: 'Вашата лична информация',                        icon: 'person',         route: '/profile',     accent: '#2563eb' }
    ]
  };

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.loadNotifications();
    if (this.showFinancials) this.loadStats();
  }

  private loadStats(): void {
    this.statsLoading = true;
    this.statsError   = '';
    this.dashSvc.getStats().pipe(
      catchError((err: HttpErrorResponse) => {
        this.statsError   = this.httpErrorMessage(err);
        this.statsLoading = false;
        this.cdr.detectChanges();
        return EMPTY;
      })
    ).subscribe(s => {
      this.stats        = s;
      this.statsLoading = false;
      this.cdr.detectChanges();
    });
  }

  private loadNotifications(): void {
    this.notifLoading = true;
    this.notifError   = '';
    this.dashSvc.getNotifications().pipe(
      catchError((err: HttpErrorResponse) => {
        this.notifError   = this.httpErrorMessage(err);
        this.notifLoading = false;
        this.cdr.detectChanges();
        return EMPTY;
      })
    ).subscribe(n => {
      this.notifications = n;
      this.notifLoading  = false;
      this.cdr.detectChanges();
    });
  }

  private httpErrorMessage(err: HttpErrorResponse): string {
    if (err.status === 0)   return 'Сървърът е недостъпен';
    if (err.status === 401) return 'Сесията е изтекла — пренасочване към вход...';
    if (err.status === 403) return 'Нямате права за тази операция';
    return `Грешка при зареждане (${err.status})`;
  }

  // ── Auth helpers ──────────────────────────────────────────────────────────
  get showFinancials(): boolean { return ['ADMIN', 'MANAGER', 'ACCOUNTANT'].includes(this.role); }
  get roleLabel():     string   { return this.roleLabels[this.role] ?? this.role; }
  get cards():         NavCard[]{ return this.allCards[this.role]   ?? [];        }

  // ── Navigation ────────────────────────────────────────────────────────────
  goTo(path: string): void { this.router.navigateByUrl(path); }

  // ── Greeting / date ───────────────────────────────────────────────────────
  get greeting(): string {
    const h = new Date().getHours();
    if (h >= 5  && h < 12) return 'Добро утро';
    if (h >= 12 && h < 18) return 'Добър ден';
    if (h >= 18 && h < 22) return 'Добър вечер';
    return 'Добра нощ';
  }
  get todayLabel(): string {
    return new Date().toLocaleDateString('bg-BG', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
  }
  get initials(): string {
    if (this.email === '—') return '?';
    const local = this.email.split('@')[0];
    const parts = local.split(/[._-]/);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return local.substring(0, 2).toUpperCase();
  }

  // ── Formatters ────────────────────────────────────────────────────────────
  fmtEur(v: number | null | undefined): string { return '€' + this.numFmt.format(v ?? 0); }
  fmtBgn(v: number | null | undefined): string { return '€' + this.numFmt.format(v ?? 0); }
  fmtSalaryEur(v: number | null | undefined): string { return '€' + this.numFmt.format(v ?? 0); }

  // ── Notification helpers ──────────────────────────────────────────────────
  get expiredNotifs():  DashboardNotification[] { return this.notifications.filter(n => n.status === 'expired');  }
  get expiringNotifs(): DashboardNotification[] { return this.notifications.filter(n => n.status === 'expiring'); }

  notifIcon(n: DashboardNotification):     string { return n.type === 'vehicle' ? 'directions_car' : 'badge'; }
  notifDaysLabel(n: DashboardNotification): string {
    const d = Math.abs(n.daysUntilExpiry);
    if (n.status === 'expired') return `Изтекло преди ${d} ${d === 1 ? 'ден' : 'дни'}`;
    return d === 0 ? 'Изтича днес!' : `Изтича след ${d} ${d === 1 ? 'ден' : 'дни'}`;
  }

  // ── Chart 1: Detailed expenses donut ─────────────────────────────────────
  readonly expenseColors  = ['#4f46e5', '#dc2626', '#ea580c', '#f59e0b'];
  readonly expenseLabels  = ['Заплати', 'Разходи курсове', 'Поддръжка', 'ДДС'];

  get expenseSegments(): DonutSegment[] {
    if (!this.stats) return [];
    const tripNoVat = Math.max(0, (this.stats.tripExpensesEur ?? 0) - (this.stats.vatThisMonthEur ?? 0));
    const vals = [
      this.stats.salaryBrutoEur   ?? 0,
      tripNoVat,
      this.stats.maintenanceEur   ?? 0,
      this.stats.vatThisMonthEur  ?? 0,
    ];
    return this.buildDonut(vals, this.expenseColors, this.expenseLabels);
  }

  get totalExpensesEur(): number {
    if (!this.stats) return 0;
    return (this.stats.salaryBrutoEur ?? 0) + (this.stats.tripExpensesEur ?? 0) + (this.stats.maintenanceEur ?? 0);
  }

  // ── Chart 2: Detailed income (last 6 months, single bars) ────────────────
  readonly INC_W = 480;
  readonly INC_H = 130;

  get incomeBarGroups() {
    const data = this.stats?.monthlyStats ?? [];
    if (!data.length) return [];
    const maxRev = Math.max(...data.map(m => m.revenueEur), 1);
    const groupW = (this.INC_W - 20) / data.length;
    const barW   = Math.min(24, groupW * 0.55);
    return data.map((m, i) => {
      const cx   = 10 + i * groupW + groupW / 2;
      const revH = (m.revenueEur / maxRev) * this.INC_H;
      return { x: cx, monthLabel: m.monthLabel, barW, revH, revY: this.INC_H - revH, revX: cx - barW / 2, revenueEur: m.revenueEur };
    });
  }

  get incomeMaxLabel(): string {
    const data = this.stats?.monthlyStats ?? [];
    const m = Math.max(...data.map(d => d.revenueEur), 0);
    return m >= 1000 ? '€' + (m / 1000).toFixed(0) + 'k' : '€' + Math.round(m);
  }

  // ── Chart 3: Income vs Expenses (last 6 months) ───────────────────────────
  readonly BAR_W = 480;
  readonly BAR_H = 130;

  get barGroups(): BarGroupData[] {
    const data = this.stats?.monthlyStats ?? [];
    if (!data.length) return [];
    const maxVal = Math.max(...data.map(m => Math.max(m.revenueEur, m.tripExpensesEur)), 1);
    const groupW = (this.BAR_W - 20) / data.length;
    const barW   = Math.min(18, groupW * 0.36);
    return data.map((m, i) => {
      const cx   = 10 + i * groupW + groupW / 2;
      const revH = (m.revenueEur      / maxVal) * this.BAR_H;
      const expH = (m.tripExpensesEur / maxVal) * this.BAR_H;
      return { x: cx, monthLabel: m.monthLabel, barW, revH, expH, revY: this.BAR_H - revH, expY: this.BAR_H - expH, revX: cx - barW - 1.5, expX: cx + 1.5, netProfit: m.netProfitEur };
    });
  }

  get barMaxLabel(): string {
    const data = this.stats?.monthlyStats ?? [];
    const m = Math.max(...data.map(d => Math.max(d.revenueEur, d.tripExpensesEur)), 0);
    return m >= 1000 ? '€' + (m / 1000).toFixed(0) + 'k' : '€' + Math.round(m);
  }

  // ── Chart 4: VAT & social insurance donut ────────────────────────────────
  readonly taxColors = ['#f59e0b', '#ef4444'];
  readonly taxLabels = ['ДДС от курсове', 'Осигуровки и данъци'];

  get taxSegments(): DonutSegment[] {
    if (!this.stats) return [];
    const vals = [
      this.stats.vatThisMonthEur  ?? 0,
      this.stats.salaryTaxesEur   ?? 0,
    ];
    return this.buildDonut(vals, this.taxColors, this.taxLabels);
  }

  get totalTaxEur(): number {
    if (!this.stats) return 0;
    return (this.stats.vatThisMonthEur ?? 0) + (this.stats.salaryTaxesEur ?? 0);
  }

  // ── Net profit line chart ─────────────────────────────────────────────────
  readonly LINE_W = 480;
  readonly LINE_H = 90;

  get linePoints(): LinePoint[] {
    const data = this.stats?.monthlyStats ?? [];
    if (data.length < 2) return [];
    const profits = data.map(m => m.netProfitEur);
    const maxAbs  = Math.max(...profits.map(Math.abs), 1);
    const midY    = this.LINE_H / 2;
    const stepX   = (this.LINE_W - 20) / (data.length - 1);
    return data.map((m, i) => ({
      x:          10 + i * stepX,
      y:          midY - (m.netProfitEur / maxAbs) * (midY - 8),
      value:      m.netProfitEur,
      monthLabel: m.monthLabel
    }));
  }

  get linePath(): string {
    return this.linePoints.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ');
  }

  get lineZeroY(): number { return this.LINE_H / 2; }

  // ── Shared donut builder ──────────────────────────────────────────────────
  private buildDonut(vals: number[], colors: string[], labels: string[]): DonutSegment[] {
    const total = vals.reduce((s, v) => s + v, 0);
    if (total <= 0) return [];
    const cx = 100, cy = 100, R = 78, r = 50;
    let angle = -Math.PI / 2;
    const segs: DonutSegment[] = [];
    vals.forEach((val, i) => {
      if (val <= 0) return;
      const sweep = (val / total) * 2 * Math.PI;
      const end   = angle + sweep;
      const large = sweep > Math.PI ? 1 : 0;
      const [x1, y1] = [cx + R * Math.cos(angle), cy + R * Math.sin(angle)];
      const [x2, y2] = [cx + R * Math.cos(end),   cy + R * Math.sin(end)];
      const [ix1,iy1]= [cx + r * Math.cos(angle), cy + r * Math.sin(angle)];
      const [ix2,iy2]= [cx + r * Math.cos(end),   cy + r * Math.sin(end)];
      const f = (n: number) => n.toFixed(2);
      segs.push({
        path: `M${f(x1)},${f(y1)} A${R},${R} 0 ${large},1 ${f(x2)},${f(y2)} L${f(ix2)},${f(iy2)} A${r},${r} 0 ${large},0 ${f(ix1)},${f(iy1)} Z`,
        color: colors[i], label: labels[i],
        pct: Math.round((val / total) * 100), value: val
      });
      angle = end;
    });
    return segs;
  }
}
