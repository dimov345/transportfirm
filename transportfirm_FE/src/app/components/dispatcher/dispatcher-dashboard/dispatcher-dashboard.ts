import {
  Component, OnInit, ChangeDetectorRef, inject, PLATFORM_ID
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';

import { VehicleService, VehicleInfo } from '../../../core/services/vehicle.service';
import { TruckGroupService, TruckGroup } from '../../../core/services/truck-group.service';
import { MaintenanceService, MaintenanceRecord } from '../../../core/services/maintenance.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-dispatcher-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dispatcher-dashboard.html',
  styleUrls: ['./dispatcher-dashboard.scss']
})
export class DispatcherDashboardComponent implements OnInit {
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  private vehicleService = inject(VehicleService);
  private truckGroupService = inject(TruckGroupService);
  private maintenanceService = inject(MaintenanceService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  loading = true;
  error = '';

  groups: TruckGroup[] = [];
  vehicles: VehicleInfo[] = [];

  selectedVehicle: VehicleInfo | null = null;
  maintenanceRecords: MaintenanceRecord[] = [];
  loadingMaintenance = false;

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
    this.loadDispatcherData();
  }

  private loadDispatcherData(): void {
    this.truckGroupService.getGroupsByDispatcherMe().subscribe({
      next: (groups) => {
        this.groups = groups;
        this.loadVehiclesFromGroups(groups);
      },
      error: () => {
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

    this.vehicleService.getAll().subscribe({
      next: (all) => {
        const groupIds = new Set(groups.map(g => g.id));
        this.vehicles = all.filter(v => v.dispatcherGroup && groupIds.has(v.dispatcherGroup.id));
        this.loading = false;

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
    return val.toFixed(2) + ' лв.';
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

  totalCost(): number {
    return this.maintenanceRecords.reduce((sum, r) => sum + (r.totalGross ?? 0), 0);
  }
}
