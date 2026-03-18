import {
  Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { EMPTY, catchError } from 'rxjs';
import { ReportService, ReportType, ReportPeriod } from '../../core/services/report.service';
import { AccountingDashboard } from '../../core/models/accounting.model';

type DataTab = 'salary' | 'trips' | 'maintenance' | 'leasing' | 'download';

interface MonthOption { value: number; label: string; }

@Component({
  selector: 'app-accounting',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './accounting.html',
  styleUrls: ['./accounting.scss']
})
export class AccountingComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  private isBrowser  = isPlatformBrowser(this.platformId);
  private cdr        = inject(ChangeDetectorRef);
  private reportSvc  = inject(ReportService);
  private router     = inject(Router);

  // ── Period state ──────────────────────────────────────────────
  activePeriod: ReportPeriod = 'MONTHLY';
  selectedYear:  number = new Date().getFullYear();
  selectedMonth: number = new Date().getMonth() + 1;
  selectedWeek:  number = this.currentIsoWeek();

  // ── Data state ────────────────────────────────────────────────
  dashboard:  AccountingDashboard | null = null;
  loading     = false;
  loadError   = '';
  downloadError = '';

  // ── UI state ──────────────────────────────────────────────────
  activeDataTab: DataTab = 'salary';

  downloading: Record<string, boolean> = {
    salary: false, trips: false, maintenance: false, leasing: false, combined: false
  };

  private readonly numFmt = new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2, maximumFractionDigits: 2
  });

  // ── Static data ───────────────────────────────────────────────
  readonly months: MonthOption[] = [
    { value:  1, label: 'Януари'    }, { value:  2, label: 'Февруари'  },
    { value:  3, label: 'Март'      }, { value:  4, label: 'Април'     },
    { value:  5, label: 'Май'       }, { value:  6, label: 'Юни'       },
    { value:  7, label: 'Юли'       }, { value:  8, label: 'Август'    },
    { value:  9, label: 'Септември' }, { value: 10, label: 'Октомври'  },
    { value: 11, label: 'Ноември'   }, { value: 12, label: 'Декември'  },
  ];

  readonly reportCards = [
    { type: 'salary'      as ReportType, icon: 'badge',          title: 'Заплати',   color: 'acc-card--blue',   description: 'Бруто / Нето / Данъци + Осигуровки за всички активни служители' },
    { type: 'trips'       as ReportType, icon: 'local_shipping', title: 'Курсове',   color: 'acc-card--green',  description: 'Приходи, разходи (ДДС, гориво, такси) и нетна печалба по курсове' },
    { type: 'maintenance' as ReportType, icon: 'build',          title: 'Поддръжка', color: 'acc-card--orange', description: 'Разходи за ремонти и сервиз на ППС с разбивка (€)' },
    { type: 'combined'    as ReportType, icon: 'summarize',      title: 'Общ отчет', color: 'acc-card--purple', description: 'Заплати + Курсове + Поддръжка + Лизинг в един файл' },
  ];

  // ── Summary tab helpers ───────────────────────────────────────────────────
  get totalExpensesEur(): number {
    if (!this.dashboard?.summary) return 0;
    const s = this.dashboard.summary;
    return (s.tripExpensesEur ?? 0) + (s.totalSalaryBrutoEur ?? 0)
         + (s.maintenanceCostsEur ?? 0) + (s.leasingTotalEur ?? 0);
  }

  expenseShare(val: number): number {
    const total = this.totalExpensesEur;
    return total > 0 ? Math.round((val / total) * 100) : 0;
  }

  // ── Lifecycle ─────────────────────────────────────────────────
  ngOnInit(): void {
    if (!this.isBrowser) return;
    this.loadData();
  }

  // ── Period controls ───────────────────────────────────────────
  setPeriod(p: ReportPeriod): void {
    this.activePeriod = p;
  }

  // ── Data loading ──────────────────────────────────────────────
  loadData(): void {
    if (!this.isBrowser) return;
    this.loading   = true;
    this.loadError = '';
    this.cdr.detectChanges();

    this.reportSvc.getData(
      this.activePeriod,
      this.selectedYear,
      this.activePeriod === 'MONTHLY' ? this.selectedMonth : undefined,
      this.activePeriod === 'WEEKLY'  ? this.selectedWeek  : undefined
    ).pipe(
      catchError((err: HttpErrorResponse) => {
        this.loadError = this.httpErrorMessage(err);
        this.loading   = false;
        this.cdr.detectChanges();
        return EMPTY;
      })
    ).subscribe(data => {
      this.dashboard = data;
      this.loading   = false;
      this.cdr.detectChanges();
    });
  }

  // ── Tab navigation ────────────────────────────────────────────
  setDataTab(tab: DataTab): void {
    this.activeDataTab = tab;
  }

  // ── Row navigation ────────────────────────────────────────────
  goToEmployee(id: string): void {
    this.router.navigate(['/employees/details', id]);
  }

  goToTrip(id: string): void {
    this.router.navigate(['/freight-trips', id]);
  }

  goToVehicle(id: string | null): void {
    if (id) this.router.navigate(['/vehicles', id]);
  }

  // ── Download ──────────────────────────────────────────────────
  download(type: ReportType): void {
    if (this.downloading[type]) return;
    this.downloading[type] = true;
    this.downloadError = '';
    this.cdr.detectChanges();

    this.reportSvc.download(
      type,
      this.activePeriod,
      this.selectedYear,
      this.activePeriod === 'MONTHLY' ? this.selectedMonth : undefined,
      this.activePeriod === 'WEEKLY'  ? this.selectedWeek  : undefined
    ).pipe(
      catchError((err: HttpErrorResponse) => {
        this.downloading[type]  = false;
        this.downloadError      = this.httpErrorMessage(err);
        this.cdr.detectChanges();
        return EMPTY;
      })
    ).subscribe(() => {
      this.downloading[type] = false;
      this.cdr.detectChanges();
    });
  }

  private httpErrorMessage(err: HttpErrorResponse): string {
    if (err.status === 0)   return 'Сървърът е недостъпен';
    if (err.status === 401) return 'Сесията е изтекла — пренасочване към вход...';
    if (err.status === 403) return 'Нямате права за тази операция';
    return `Грешка (${err.status})`;
  }

  // ── Formatters ────────────────────────────────────────────────
  fmtEur(val: number | null | undefined): string {
    return '€' + this.numFmt.format(val ?? 0);
  }

  fmtBgn(val: number | null | undefined): string {
    return '€' + this.numFmt.format(val ?? 0);
  }

  /** Positive → green "+€X", negative → red "-€X" */
  fmtSignedEur(val: number | null | undefined): string {
    const v = val ?? 0;
    return (v >= 0 ? '+' : '-') + '€' + this.numFmt.format(Math.abs(v));
  }

  /** Always renders as expense with minus prefix */
  fmtExpEur(val: number | null | undefined): string {
    return '-€' + this.numFmt.format(val ?? 0);
  }

  fmtExpBgn(val: number | null | undefined): string {
    return '-€' + this.numFmt.format(val ?? 0);
  }

  // ── Trip status helpers ───────────────────────────────────────
  tripStatusLabel(status: string | null): string {
    switch (status) {
      case 'PLANNED':    return 'Планиран';
      case 'IN_TRANSIT': return 'В движение';
      case 'COMPLETED':  return 'Завършен';
      default:           return status ?? '';
    }
  }

  tripStatusClass(status: string | null): string {
    switch (status) {
      case 'PLANNED':    return 'status-badge--planned';
      case 'IN_TRANSIT': return 'status-badge--transit';
      case 'COMPLETED':  return 'status-badge--completed';
      default:           return '';
    }
  }

  // ── Computed helpers ──────────────────────────────────────────
  get periodSummary(): string {
    if (this.activePeriod === 'WEEKLY')  return `Седмица ${this.selectedWeek} / ${this.selectedYear}`;
    if (this.activePeriod === 'MONTHLY') return `${this.months[this.selectedMonth - 1]?.label ?? ''} ${this.selectedYear}`;
    return `Година ${this.selectedYear}`;
  }

  isNetPositive(): boolean {
    return (this.dashboard?.summary?.netBalanceEur ?? 0) >= 0;
  }

  private currentIsoWeek(): number {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    d.setDate(d.getDate() + 3 - (d.getDay() + 6) % 7);
    const w1 = new Date(d.getFullYear(), 0, 4);
    return 1 + Math.round(((d.getTime() - w1.getTime()) / 86400000 - 3 + (w1.getDay() + 6) % 7) / 7);
  }
}
