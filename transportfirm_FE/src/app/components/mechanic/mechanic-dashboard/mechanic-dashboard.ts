import {
  Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';

import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';
import { TruckGroupService, TruckGroup } from '../../../core/services/truck-group.service';
import { MaintenanceService, MaintenanceRecord } from '../../../core/services/maintenance.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-mechanic-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mechanic-dashboard.html',
  styleUrls: ['./mechanic-dashboard.scss']
})
export class MechanicDashboardComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  private vehicleService = inject(VehicleService);
  private truckGroupService = inject(TruckGroupService);
  private maintenanceService = inject(MaintenanceService);
  private employeeService = inject(EmployeeService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  // State
  loading = true;
  error = '';
  mechanicName = '';
  mechanicId = '';

  groups: TruckGroup[] = [];
  vehicles: VehicleInfo[] = [];

  selectedVehicle: VehicleInfo | null = null;
  maintenanceRecords: MaintenanceRecord[] = [];
  loadingMaintenance = false;

  // Expanded record for details
  expandedRecordId: string | null = null;

  readonly maintenanceTypeLabels: Record<string, string> = {
    REPAIR: 'Ремонт',
    SERVICE: 'Сервиз',
    INSPECTION: 'Технически преглед',
    TIRE_CHANGE: 'Смяна на гуми',
    BREAKDOWN: 'Авария',
    ACCIDENT: 'Катастрофа',
    OTHER: 'Друго'
  };

  readonly statusLabels: Record<string, string> = {
    OPEN: 'Отворен',
    CLOSED: 'Затворен',
    CANCELED: 'Отменен'
  };

  readonly statusClasses: Record<string, string> = {
    OPEN: 'status-open',
    CLOSED: 'status-closed',
    CANCELED: 'status-canceled'
  };

  ngOnInit(): void {
    if (!this.isBrowser) return;
    this.loadMechanicData();
  }

  private loadMechanicData(): void {
    const email = this.authService.getEmail();
    if (!email) {
      this.error = 'Не е намерен потребител.';
      this.loading = false;
      return;
    }

    // GET /mechanics/me/groups — вземаме групите на логнатия механик
    this.truckGroupService.getGroupsByMechanicMe().subscribe({
      next: (groups) => {
        this.groups = groups;
        this.mechanicName = email;

        // Вземаме всички ППС от всички групи
        this.loadVehiclesFromGroups(groups);
      },
      error: (err) => {
        this.error = 'Грешка при зареждане на данните.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private loadVehiclesFromGroups(groups: TruckGroup[]): void {
    if (groups.length === 0) {
      this.vehicles = [];
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }

    // Вземаме всички ППС и филтрираме по mechanic groups
    this.vehicleService.getAll().subscribe({
      next: (all) => {
        const groupIds = new Set(groups.map(g => g.id));
        this.vehicles = all.filter(v => v.mechanicGroup && groupIds.has(v.mechanicGroup.id));
        this.loading = false;

        // Автоматично избираме първото ППС
        if (this.vehicles.length > 0) {
          this.selectVehicle(this.vehicles[0]);
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Грешка при зареждане на ППС.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  selectVehicle(vehicle: VehicleInfo): void {
    this.selectedVehicle = vehicle;
    this.expandedRecordId = null;
    this.loadMaintenance(vehicle.id);
  }

  private loadMaintenance(vehicleId: string): void {
    this.loadingMaintenance = true;
    this.maintenanceRecords = [];
    this.cdr.detectChanges();

    this.maintenanceService.getByVehicle(vehicleId, 0, 50).subscribe({
      next: (page) => {
        this.maintenanceRecords = page.content;
        this.loadingMaintenance = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingMaintenance = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleRecord(recordId: string): void {
    this.expandedRecordId = this.expandedRecordId === recordId ? null : recordId;
  }

  newMaintenance(): void {
    if (!this.selectedVehicle) return;
    this.router.navigate(['/mechanic/maintenance/new', this.selectedVehicle.id]);
  }

  editMaintenance(recordId: string): void {
    this.router.navigate(['/mechanic/maintenance/edit', recordId]);
  }

  closeRecord(record: MaintenanceRecord): void {
    if (!confirm(`Затвори запис "${this.maintenanceTypeLabels[record.type]}"?`)) return;

    this.maintenanceService.close(record.id).subscribe({
      next: (updated) => {
        const idx = this.maintenanceRecords.findIndex(r => r.id === record.id);
        if (idx !== -1) this.maintenanceRecords[idx] = updated;
        this.cdr.detectChanges();
      },
      error: () => alert('Грешка при затваряне на записа.')
    });
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('bg-BG');
  }

  formatDateTime(dateStr?: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleString('bg-BG');
  }

  formatMoney(val?: number): string {
    if (val == null) return '—';
    return '€' + val.toFixed(2);
  }

  isExpired(dateStr?: string): boolean {
    if (!dateStr) return false;
    return new Date(dateStr) < new Date();
  }

  getVehicleStatusLabel(value?: string | null): string {
    const map: Record<string, string> = {
      AVAILABLE: 'Свободно',
      ON_ROUTE: 'На курс',
      IN_SERVICE: 'В сервиз',
      BROKEN_DOWN: 'Повредено',
      WAITING_PARTS: 'Чака части',
      OUT_OF_SERVICE: 'Извън експлоатация'
    };
    return value ? (map[value] ?? value) : '—';
  }

  getVehicleStatusClass(value?: string | null): string {
    const map: Record<string, string> = {
      AVAILABLE: 'vs-available',
      ON_ROUTE: 'vs-on-route',
      IN_SERVICE: 'vs-in-service',
      BROKEN_DOWN: 'vs-broken',
      WAITING_PARTS: 'vs-waiting',
      OUT_OF_SERVICE: 'vs-out'
    };
    return value ? (map[value] ?? '') : '';
  }

  get vehiclesByGroup(): { group: TruckGroup; vehicles: VehicleInfo[] }[] {
    return this.groups
      .map(g => ({
        group: g,
        vehicles: this.vehicles.filter(v => v.mechanicGroup?.id === g.id)
      }))
      .filter(e => e.vehicles.length > 0);
  }

  openRecords(): number {
    return this.maintenanceRecords.filter(r => r.status === 'OPEN').length;
  }

  totalCost(): number {
    return this.maintenanceRecords.reduce((sum, r) => sum + (r.totalGross ?? 0), 0);
  }
}
